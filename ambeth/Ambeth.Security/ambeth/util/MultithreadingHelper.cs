using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Threading;
using System;
using System.Collections.Generic;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
    public class MultithreadingHelper : IMultithreadingHelper
    {
        public const String TIMEOUT = "ambeth.mth.timeout";

        [Autowired]
        public IServiceContext BeanContext { protected get; set; }

        [Autowired(Optional = true)]
        public IThreadPool ThreadPool { protected get; set; }

        [Autowired]
        public IThreadLocalCleanupController ThreadLocalCleanupController { protected get; set; }

        [Property(TIMEOUT, DefaultValue = "30000")]
        public long Timeout { protected get; set; }

        public void InvokeInParallel(IServiceContext serviceContext, Runnable runnable, int workerCount)
        {
            InvokeInParallel(serviceContext, false, runnable, workerCount);
        }

        public void InvokeInParallel(IServiceContext serviceContext, bool inheritThreadLocals, Runnable runnable, int workerCount)
        {
            Runnable[] runnables = new Runnable[workerCount];
            for (int a = workerCount; a-- > 0; )
            {
                if (runnable is INamedRunnable)
                {
                    String name = ((INamedRunnable)runnable).Name + "-" + a;
                    runnables[a] = new WrappingNamedRunnable(runnable, name);
                }
                else
                {
                    runnables[a] = runnable;
                }
            }
            InvokeInParallel(serviceContext, inheritThreadLocals, runnables);
        }

        public void InvokeInParallel(IServiceContext serviceContext, params Runnable[] runnables)
        {
            InvokeInParallel(serviceContext, false, runnables);
        }

        public void InvokeInParallel(IServiceContext serviceContext, bool inheritThreadLocals, params Runnable[] runnables)
        {
            CountDownLatch latch = new CountDownLatch(runnables.Length);
            ParamHolder<Exception> throwableHolder = new ParamHolder<Exception>();
            IForkState forkState = inheritThreadLocals ? ThreadLocalCleanupController.CreateForkState() : null;

            Thread[] threads = new Thread[runnables.Length];
            for (int a = runnables.Length; a-- > 0; )
            {
                Runnable catchingRunnable = BeanContext.RegisterBean<CatchingRunnable>()//
                    .PropertyValue("Runnable", runnables[a])//
                    .PropertyValue("Latch", latch)//
                    .PropertyValue("ForkState", forkState)//
                    .PropertyValue("ThrowableHolder", throwableHolder).Finish();

                Thread thread = new Thread(delegate()
                    {
                        catchingRunnable.Run();
                    });
                thread.IsBackground = true;
                threads[a] = thread;
            }
            foreach (Thread thread in threads)
            {
                thread.Start();
            }
            latch.Await(TimeSpan.FromMilliseconds(Timeout));
            if (throwableHolder.Value != null)
            {
                throw RuntimeExceptionUtil.Mask(throwableHolder.Value, "Error occured while invoking runnables");
            }
        }

#if !SILVERLIGHT
        public void InvokeAndWait<R, V>(IList<V> items, IResultingBackgroundWorkerParamDelegate<R, V> itemHandler,
            IAggregrateResultHandler<R, V> aggregateResultHandler)
        {
            if (items.Count == 0)
            {
                return;
            }
            if (items.Count == 1 || ThreadPool == null)
            {
                for (int a = items.Count; a-- > 0; )
                {
                    V item = items[a];
                    items.RemoveAt(items.Count - 1);
                    R result = itemHandler(item);
                    aggregateResultHandler(result, item);
                }
                return;
            }
            RunnableHandle<R, V> runnableHandle = new RunnableHandle<R, V>(itemHandler, aggregateResultHandler, items, ThreadLocalCleanupController);

            Runnable parallelRunnable = new ParallelRunnable<R, V>(runnableHandle, true);
            Runnable mainRunnable = new ParallelRunnable<R, V>(runnableHandle, false);

            // for n items fork at most n - 1 threads because our main thread behaves like a worker by itself
            for (int a = items.Count - 1; a-- > 0; )
            {
                ThreadPool.Queue(delegate()
                {
                    parallelRunnable.Run();
                });
            }
            // consume items with the "main thread" as long as there is one in the queue
            mainRunnable.Run();

            // wait till the forked threads have finished, too
            WaitForLatch(runnableHandle.latch, runnableHandle.exHolder);
        }

        protected void WaitForLatch(CountDownLatch latch, IParamHolder<Exception> exHolder)
        {
            while (true)
            {
                if (exHolder.Value != null)
                {
                    // A parallel exception will be thrown here

                    Exception ex = exHolder.Value;
                    throw ex;
                }
                try
                {
                    latch.Await();
                    if (latch.GetCount() == 0)
                    {
                        return;
                    }
                }
                catch (ThreadInterruptedException)
                {
                    // intended blank
                }
            }
        }
#endif
    }
}