using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Threading;
using System;
using System.Collections.Generic;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
#if !SILVERLIGHT
    public class ResultingParallelRunnable<R, V> : Runnable
    {
        protected readonly ResultingRunnableHandle<R, V> runnableHandle;

        protected readonly bool buildThreadLocals;

        public ResultingParallelRunnable(ResultingRunnableHandle<R, V> runnableHandle, bool buildThreadLocals)
        {
            this.runnableHandle = runnableHandle;
            this.buildThreadLocals = buildThreadLocals;
        }

        public void Run()
        {
            try
            {
                Thread currentThread = Thread.CurrentThread;
                String oldName = currentThread.Name;
                if (buildThreadLocals)
                {
                    String name = runnableHandle.createdThread.Name;
                    currentThread.Name = name + " " + oldName;
                }
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
                            item = items[items.Count - 1];
                            items.RemoveAt(items.Count - 1);
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
                        currentThread.Name = oldName;
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