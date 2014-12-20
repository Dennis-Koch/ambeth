using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Threading;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Util
{
#if !SILVERLIGHT
    public class ParallelRunnable<R, V> : Runnable
    {
        protected readonly RunnableHandle<R, V> runnableHandle;

        protected readonly bool buildThreadLocals;

        public ParallelRunnable(RunnableHandle<R, V> runnableHandle, bool buildThreadLocals)
        {
            this.runnableHandle = runnableHandle;
            this.buildThreadLocals = buildThreadLocals;
        }

        public void Run()
        {
            try
            {
                Object parallelLock = runnableHandle.parallelLock;
                IList<V> items = runnableHandle.items;
                IForkState forkState = runnableHandle.forkState;
                ParamHolder<Exception> exHolder = runnableHandle.exHolder;
                CountDownLatch latch = runnableHandle.latch;

                IBackgroundWorkerParamDelegate<V> run = new IBackgroundWorkerParamDelegate<V>(delegate(V item)
                {
                    R result = runnableHandle.run(item);
                    IAggregrateResultHandler<R, V> aggregrateResultHandler = runnableHandle.aggregrateResultHandler;
                    if (aggregrateResultHandler != null)
                    {
                        lock (parallelLock)
                        {
                            aggregrateResultHandler(result, item);
                        }
                    }
                });

                while (true)
                {
                    V item;
                    lock (parallelLock)
                    {
                        if (exHolder.Value != null)
                        {
                            // an uncatched error occurred somewhere
                            return;
                        }
                        // pop the last item of the queue
                        if (items.Count > 0)
                        {
                            item = items[items.Count - 1];
                            items.RemoveAt(items.Count - 1);
                        }
                        else
                        {
                            item = default(V);
                        }
                    }
                    if (item == null)
                    {
                        // queue finished
                        return;
                    }
                    try
                    {
                        if (buildThreadLocals)
                        {
                            forkState.Use(run, item);
                        }
                        else
                        {
                            run(item);
                        }
                    }
                    catch (Exception e)
                    {
                        lock (parallelLock)
                        {
                            if (exHolder.Value == null)
                            {
                                exHolder.Value = e;
                            }
                        }
                    }
                    finally
                    {
                        latch.CountDown();
                    }
                }
            }
            finally
            {
                if (buildThreadLocals)
                {
                    runnableHandle.threadLocalCleanupController.CleanupThreadLocal();
                }
            }
        }
    }
#endif
}