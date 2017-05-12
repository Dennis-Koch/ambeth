using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Threading;
using System;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
    public class CatchingRunnable : Runnable
    {
        protected readonly IForkState forkState;

	    protected readonly Runnable runnable;

	    protected readonly CountDownLatch latch;

	    protected readonly IParamHolder<Exception> throwableHolder;

        protected readonly IThreadLocalCleanupController threadLocalCleanupController;

        public CatchingRunnable(IForkState forkState, Runnable runnable, CountDownLatch latch, IParamHolder<Exception> throwableHolder,
			    IThreadLocalCleanupController threadLocalCleanupController)
	    {
		    this.forkState = forkState;
		    this.runnable = runnable;
		    this.latch = latch;
		    this.throwableHolder = throwableHolder;
		    this.threadLocalCleanupController = threadLocalCleanupController;
	    }
       
        public void Run()
        {
            Thread currentThread = Thread.CurrentThread;
            String oldName = currentThread.Name;
            if (runnable is INamedRunnable)
            {
                currentThread.Name = ((INamedRunnable)runnable).Name;
            }
            try
            {
                try
                {
                    if (forkState != null)
                    {
                        forkState.Use(runnable);
                    }
                    else
                    {
                        runnable.Run();
                    }
                }
                catch (Exception e)
                {
                    throwableHolder.Value = e;
                }
                finally
                {
                    threadLocalCleanupController.CleanupThreadLocal();
                    latch.CountDown();
                }
            }
            finally
            {
                currentThread.Name = oldName;
            }
        }
    }
}