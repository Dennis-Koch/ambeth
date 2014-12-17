using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Threading;
using System;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
    public class MultithreadingHelper : IMultithreadingHelper
    {
        public const String TIMEOUT = "ambeth.mth.timeout";

        [Autowired]
        public IServiceContext BeanContext { protected get; set; }

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
    }
}