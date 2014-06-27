using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Threadlocal;
#if SILVERLIGHT
using Castle.Core.Interceptor;
#else
using Castle.DynamicProxy;
#endif
using De.Osthus.Ambeth.Log;
using System.Reflection;
using System;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Service;
using System.Threading;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Cache.Interceptor
{
    public class ThreadLocalRootCacheInterceptor : AbstractRootCacheAwareInterceptor, IInterceptor
    {
        [LogInstance]
        public new ILogger Log { private get; set; }

        [Property(DefaultValue = "false")]
        public bool privileged { protected get; set; }

        protected readonly ThreadLocal<RootCache> rootCacheTL = new ThreadLocal<RootCache>();

        protected IRootCache GetCurrentRootCache()
        {
            IRootCache rootCache = GetCurrentRootCacheIfValid();
            if (rootCache == null)
            {
                rootCache = AcquireRootCache(privileged, rootCacheTL);
            }
            return rootCache;
        }

        protected IRootCache GetCurrentRootCacheIfValid()
        {
            return rootCacheTL.Value;
        }

        public void Intercept(IInvocation invocation)
        {
            if (clearMethod.Equals(invocation.Method))
            {
                IRootCache rootCache = GetCurrentRootCacheIfValid();
                if (rootCache == null)
                {
                    // Nothing to do
                    return;
                }
            }
            IRootCache rootCache2 = GetCurrentRootCache();
            invocation.ReturnValue = invocation.Method.Invoke(rootCache2, invocation.Arguments);
        }

        public override void CleanupThreadLocal()
        {
            DisposeCurrentRootCache(rootCacheTL);
        }
    }
}