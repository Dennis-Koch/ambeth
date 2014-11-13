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
    public class CatchingRunnable : Runnable, IInitializingBean
    {
        [Property]
        public Runnable Runnable { protected get; set; }

        [Property]
        public CountDownLatch latch { protected get; set; }

        [Property]
        public IParamHolder<Exception> throwableHolder { protected get; set; }

        [Autowired]
        public ISecurityContextHolder securityContextHolder { protected get; set; }

        [Autowired]
        public ISecurityScopeProvider securityScopeProvider { protected get; set; }

        [Autowired]
        public IThreadLocalCleanupController threadLocalCleanupController { protected get; set; }

        protected IAuthentication authentication;

        protected IAuthorization authorization;

        protected ISecurityScope[] securityScopes;

        public void AfterPropertiesSet()
        {
            ISecurityContext securityContext = securityContextHolder.Context;
            if (securityContext != null)
            {
                authentication = securityContext.Authentication;
                authorization = securityContext.Authorization;
            }
            securityScopes = securityScopeProvider.SecurityScopes;
        }

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
                if (authentication != null)
                {
                    ISecurityContext contextOfThread = securityContextHolder.GetCreateContext();
                    contextOfThread.Authentication = authentication;
                }
                if (authorization != null)
                {
                    ISecurityContext contextOfThread = securityContextHolder.GetCreateContext();
                    contextOfThread.Authorization = authorization;
                }
                try
                {
                    securityScopeProvider.ExecuteWithSecurityScopes<Object>(delegate()
                    {
                        Runnable.Run();
                        return null;
                    }, securityScopes);
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