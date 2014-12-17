using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Threading;
using System;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
    public class CatchingRunnable : Runnable
    {
        [Property]
        public IForkState ForkState { protected get; set; }

        [Property]
        public Runnable Runnable { protected get; set; }

        [Property]
        public CountDownLatch latch { protected get; set; }

        [Property]
        public IParamHolder<Exception> throwableHolder { protected get; set; }

        [Autowired]
        public ISecurityContextHolder securityContextHolder { protected get; set; }

        [Autowired]
        public IThreadLocalCleanupController threadLocalCleanupController { protected get; set; }
       
        public void Run()
        {
            Thread currentThread = Thread.CurrentThread;
            String oldName = currentThread.Name;
            if (Runnable is INamedRunnable)
            {
                currentThread.Name = ((INamedRunnable)Runnable).Name;
            }
            try
            {
                try
                {
                    if (ForkState != null)
                    {
                        ForkState.Use(Runnable);
                    }
                    else
                    {
                        Runnable.Run();
                    }
                }
                catch (Exception e)
                {
                    throwableHolder.Value = e;
                }
                finally
                {
                    if (threadLocalCleanupController != null)
                    {
                        threadLocalCleanupController.CleanupThreadLocal();
                    }
                    latch.CountDown();
                    securityContextHolder.ClearContext();
                }
            }
            finally
            {
                currentThread.Name = oldName;
            }
        }
    }
}