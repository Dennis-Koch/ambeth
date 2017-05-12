using System.ServiceModel;
using System.Threading;
using De.Osthus.Ambeth.Ioc;
using System;
using De.Osthus.Ambeth.Log;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Util;
using System.Collections;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Threading
{
    public class ThreadPool : IThreadPool, IInitializingBean
    {        
        public class QueueGroup
        {
            public DateTime ExecutionTime { get; private set; }

            public AbstractQueueGroupKey QueueGroupKey { get; private set; }

            public IList Queue { get; private set; }

            public QueueGroup(AbstractQueueGroupKey queueGroupKey)
            {
                this.QueueGroupKey = queueGroupKey;
                ExecutionTime = DateTime.Now + TimeSpan.FromMilliseconds(queueGroupKey.QueueInterval);
            }

            public QueueGroup(AbstractQueueGroupKey queueGroupKey, IList queue)
            {
                this.QueueGroupKey = queueGroupKey;
                this.Queue = queue;
                ExecutionTime = DateTime.Now + TimeSpan.FromMilliseconds(queueGroupKey.QueueInterval);
            }
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly IDictionary<AbstractQueueGroupKey, QueueGroup> queueGroupKeyToExecutionTimeDict = new IdentityDictionary<AbstractQueueGroupKey, QueueGroup>();

        protected readonly IDictionary<AbstractQueueGroupKey, IList<QueueGroup>> queueGroupKeyToExecuteDict = new IdentityDictionary<AbstractQueueGroupKey, IList<QueueGroup>>();

        protected readonly IList<QueueGroup> pendingQueueGroups = new List<QueueGroup>();

        public SynchronizationContext SyncContext { protected get; set; }

        public TimeSpan CheckInterval { protected get; set; }

        [Autowired]
        public IThreadLocalCleanupController ThreadLocalCleanupController { protected get; set; }

        public ThreadPool()
        {
            CheckInterval = TimeSpan.FromMilliseconds(50);
        }

        public virtual void AfterPropertiesSet()
        {
            // Start a perma-background thread to check for QueueGroups ready to fire
            Queue(CheckForPendingQueueRunnable);
        }

        protected void CheckForPendingQueueRunnable()
        {
            while (true)
            {
                CheckForPendingQueues();
                Thread.Sleep(CheckInterval);
            }
        }

        public virtual void Queue(IBackgroundWorkerDelegate workerRunnable)
        {
            System.Threading.ThreadPool.QueueUserWorkItem(delegate(Object state)
            {
#if SILVERLIGHT
                try
                {
                    workerRunnable.Invoke();
                }
                catch (ThreadAbortException)
                {
                    // Intended blank
                }
                catch (Exception e)
                {
                    Log.Error(e);
                    throw;
                }
#else
                try
                {
	                workerRunnable.Invoke();
                }
                catch (ThreadAbortException)
                {
                    // Intended blank
                }
                catch (Exception e)
                {
                    Log.Error(e);
                }
#endif
                finally
                {
                    ThreadLocalCleanupController.CleanupThreadLocal();
                }
            });
        }

        public virtual void Queue<T>(IBackgroundWorkerParamDelegate<T> workerRunnable, T state)
        {
            System.Threading.ThreadPool.QueueUserWorkItem(delegate(Object myState)
            {
                try
                {
                    workerRunnable.Invoke(state);
                }
                catch (ThreadAbortException)
                {
                    // Intended blank
                }
#if SILVERLIGHT
                catch (Exception e)
                {
                    Log.Error(e);
                    throw;
                }
#else
                catch (Exception e)
                {
                    Log.Error(e);
                }
#endif
                finally
                {
                    ThreadLocalCleanupController.CleanupThreadLocal();
                }
            });
        }

        public virtual void Queue(WaitCallback waitCallback, Object state)
        {
            System.Threading.ThreadPool.QueueUserWorkItem(delegate(Object myState)
            {
                try
                {
                    waitCallback.Invoke(myState);
                }
                catch (ThreadAbortException)
                {
                    // Intended blank
                }
#if SILVERLIGHT
                catch (Exception e)
                {
                    Log.Error(e);
                    throw;
                }
#else
                catch (Exception e)
                {
                    Log.Error(e);
                }
#endif          
                finally
                {
                    ThreadLocalCleanupController.CleanupThreadLocal();
                }
            }, state);
        }

        private void CheckForPendingQueues()
        {
            lock (queueGroupKeyToExecutionTimeDict)
            {
                DateTime now = DateTime.Now;
                for (int a = pendingQueueGroups.Count; a-- > 0; )
                {
                    // Look at the last QueueGroup
                    QueueGroup queueGroup = pendingQueueGroups[a];
                    if (now < queueGroup.ExecutionTime)
                    {
                        // If the next QueueGroup is not ready, the others won't be, too
                        break;
                    }
                    pendingQueueGroups.RemoveAt(a);
                    AbstractQueueGroupKey queueGroupKey = queueGroup.QueueGroupKey;
                    queueGroupKeyToExecutionTimeDict.Remove(queueGroupKey);

                    lock (queueGroupKeyToExecuteDict)
                    {
                        IList<QueueGroup> currentExecutedQueueGroups = DictionaryExtension.ValueOrDefault(queueGroupKeyToExecuteDict, queueGroupKey);
                        if (currentExecutedQueueGroups != null)
                        {
                            // There is already a thread executing the key. Nothing to do here
                            currentExecutedQueueGroups.Add(queueGroup);
                            return;
                        }
                        // List container for concurrent QueueGroup of the same key
                        currentExecutedQueueGroups = new List<QueueGroup>();
                        queueGroupKeyToExecuteDict[queueGroupKey] = currentExecutedQueueGroups;
                    }
                    if (queueGroup.QueueGroupKey.InvokeFromGuiThread && SyncContext != null)
                    {
                        SyncContext.Post(ExecuteQueuedDelegate, queueGroup);
                        continue;
                    }
                    Queue(delegate()
                    {
                        ExecuteQueuedDelegate(queueGroup);
                    });
                }
            }
        }

        private void ExecuteQueuedDelegate(Object state)
        {
            QueueGroup queueGroup = (QueueGroup)state;
            AbstractQueueGroupKey queueGroupKey = queueGroup.QueueGroupKey;
                       
            bool doRun = true;
            while (doRun)
            {
                try
                {
                    if (queueGroupKey is QueueGroupKey)
                    {
                        ((QueueGroupKey)queueGroupKey).QueuedDelegate.Invoke();
                    }
                    else
                    {
                        Delegate delegateObj = (Delegate)queueGroupKey.GetType().GetProperty("QueuedDelegate").GetValue(queueGroupKey, null);
                        delegateObj.DynamicInvoke(queueGroup.Queue);
                    }
                }
                finally
                {
                    lock (queueGroupKeyToExecuteDict)
                    {
                        IList<QueueGroup> currentExecutedQueueGroups = DictionaryExtension.ValueOrDefault(queueGroupKeyToExecuteDict, queueGroupKey);
                        if (currentExecutedQueueGroups.Count > 0)
                        {
                            // There is another queueGroup of the same key ready to be executed. Do it now immediately
                            queueGroup = currentExecutedQueueGroups[0];
                            queueGroupKey = queueGroup.QueueGroupKey;
                            currentExecutedQueueGroups.RemoveAt(0);
                        }
                        else
                        {
                            queueGroupKeyToExecuteDict.Remove(queueGroupKey);
                            doRun = false;
                        }
                    }
                }
            }
        }

        public void Queue<T>(QueueGroupKey<T> queueGroupKey, T item)
        {
            lock (queueGroupKeyToExecutionTimeDict)
            {
                QueueGroup queueGroup = DictionaryExtension.ValueOrDefault(queueGroupKeyToExecutionTimeDict, queueGroupKey);
                if (queueGroup != null)
                {
                    // Runnable already pending with given QueueGroupKey
                    queueGroup.Queue.Add(item);
                    return;
                }
                queueGroup = new QueueGroup(queueGroupKey, new List<T>());
                queueGroupKeyToExecutionTimeDict.Add(queueGroupKey, queueGroup);

                queueGroup.Queue.Add(item);
                QueueIntern<T>(queueGroup);
            }
        }

        public void Queue<T>(QueueGroupKey<T> queueGroupKey, IEnumerable<T> items)
        {
            lock (queueGroupKeyToExecutionTimeDict)
            {
                QueueGroup queueGroup = DictionaryExtension.ValueOrDefault(queueGroupKeyToExecutionTimeDict, queueGroupKey);
                if (queueGroup != null)
                {
                    // Runnable already pending with given QueueGroupKey
                    IList queue2 = queueGroup.Queue;
                    foreach (T item in items)
                    {
                        queue2.Add(item);
                    }
                    return;
                }
                queueGroup = new QueueGroup(queueGroupKey, new List<T>());
                queueGroupKeyToExecutionTimeDict.Add(queueGroupKey, queueGroup);

                IList queue = queueGroup.Queue;
                foreach (T item in items)
                {
                    queue.Add(item);
                }
                QueueIntern<T>(queueGroup);
            }
        }

        protected void QueueIntern<T>(QueueGroup queueGroup)
        {
            bool queueGroupAdded = false;

            // Insert the QueueGroup at the right position of pending QueueGroups
            // At index 0 is the latest of all, at max-index the next triggering QueueGroup
            for (int a = 0, size = pendingQueueGroups.Count; a < size; a++)
            {
                QueueGroup pendingQueueGroup = pendingQueueGroups[a];
                if (queueGroup.ExecutionTime >= pendingQueueGroup.ExecutionTime)
                {
                    // If new QueueGroup executes AFTER or EQUAL TO the current investigated QueueGroup,
                    // insert the new QueueGroup BEFORE in the list
                    pendingQueueGroups.Insert(a, queueGroup);
                    queueGroupAdded = true;
                    break;
                }
            }
            if (!queueGroupAdded)
            {
                pendingQueueGroups.Add(queueGroup);
            }
        }

        public void Queue(QueueGroupKey queueGroupKey)
        {
            lock (queueGroupKeyToExecutionTimeDict)
            {
                QueueGroup queueGroup = DictionaryExtension.ValueOrDefault(queueGroupKeyToExecutionTimeDict, queueGroupKey);
                if (queueGroup != null)
                {
                    // Runnable already pending with given QueueGroupKey
                    return;
                }
                queueGroup = new QueueGroup(queueGroupKey);

                queueGroupKeyToExecutionTimeDict.Add(queueGroupKey, queueGroup);
                bool queueGroupAdded = false;

                // Insert the QueueGroup at the right position of pending QueueGroups
                // At index 0 is the latest of all, at max-index the next triggering QueueGroup
                for (int a = 0, size = pendingQueueGroups.Count; a < size; a++)
                {
                    QueueGroup pendingQueueGroup = pendingQueueGroups[a];
                    if (queueGroup.ExecutionTime >= pendingQueueGroup.ExecutionTime)
                    {
                        // If new QueueGroup executes AFTER or EQUAL TO the current investigated QueueGroup,
                        // insert the new QueueGroup BEFORE in the list
                        pendingQueueGroups.Insert(a, queueGroup);
                        queueGroupAdded = true;
                        break;
                    }
                }
                if (!queueGroupAdded)
                {
                    pendingQueueGroups.Add(queueGroup);
                }
            }
        }

        public void Queue<T>(long yieldingInterval, ExecuteYieldingDelegate<T> executeYieldingDelegate, T item)
        {
            YieldingController yieldingController;
            if (SyncContext == null)
            {
                yieldingController = new YieldingController(0);
                while (!executeYieldingDelegate.Invoke(item, yieldingController))
                {
                    // Loop till delegate tells us that is has been finished
                }
                return;
            }
            yieldingController = new YieldingController(yieldingInterval);
            SyncContext.Post(ExecuteYieldingDelegate, new Object[] { executeYieldingDelegate, yieldingController, item });
        }

        protected void ExecuteYieldingDelegate(Object state)
        {
            Object[] stateArray = (Object[])state;
            Delegate executeYieldingDelegate = (Delegate)stateArray[0];
            YieldingController yieldingController = (YieldingController)stateArray[1];
            Object item = stateArray[2];

            // Calculate time when the delegate should yield its action
            yieldingController.CalculateNextInterval();
            if ((bool)executeYieldingDelegate.DynamicInvoke(item, yieldingController))
            {
                // Execution finished by decision of the delegate
                return;
            }
            // Re-queue action in the UI events
            SyncContext.Post(ExecuteYieldingDelegate, state);
        }
    }
}
