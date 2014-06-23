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

namespace De.Osthus.Ambeth.Cache.Interceptor
{
    public class ThreadLocalRootCacheInterceptor : IInitializingBean, IInterceptor, IThreadLocalCleanupBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly ThreadLocal<RootCache> rootCacheTL = new ThreadLocal<RootCache>();

        public ICacheRetriever StoredCacheRetriever { protected get; set; }

        public IOfflineListenerExtendable OfflineListenerExtendable { protected get; set; }

        public IServiceContext ServiceContext { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ServiceContext, "ServiceContext");
            ParamChecker.AssertNotNull(StoredCacheRetriever, "StoredCacheRetriever");
        }

        protected IRootCache GetCurrentRootCache()
        {
            IRootCache rootCache = GetCurrentRootCacheIfValid();
            if (rootCache == null)
            {
                rootCache = AcquireCurrentRootCache();
            }
            return rootCache;
        }

        protected virtual IRootCache AcquireCurrentRootCache()
        {
            IBeanRuntime<RootCache> rootCacheBR = ServiceContext.RegisterAnonymousBean<RootCache>().PropertyValue("CacheRetriever", StoredCacheRetriever);
            // Do not inject EventQueue because caches without foreign interest will never receive async DCEs
            RootCache rootCache = PostProcessRootCacheConfiguration(rootCacheBR).Finish();

            if (OfflineListenerExtendable != null)
            {
                OfflineListenerExtendable.AddOfflineListener(rootCache);
            }
            rootCacheTL.Value = rootCache;
            return rootCache;
        }

        protected virtual IBeanRuntime<RootCache> PostProcessRootCacheConfiguration(IBeanRuntime<RootCache> rootCacheBR)
        {
            // Do not inject EventQueue because caches without foreign interest will never receive async DCEs
            return rootCacheBR.IgnoreProperties("EventQueue").PropertyValue("WeakEntries", false);
        }

        protected virtual IRootCache GetCurrentRootCacheIfValid()
        {
            return rootCacheTL.Value;
        }

        public void Intercept(IInvocation invocation)
        {
            IRootCache rootCache = GetCurrentRootCache();
            invocation.ReturnValue = invocation.Method.Invoke(rootCache, invocation.Arguments);
        }

        public virtual void CleanupThreadLocal()
        {
            DisposeCurrentRootCache();
        }

        protected virtual void DisposeCurrentRootCache()
        {
            RootCache rootCache = rootCacheTL.Value;
            if (rootCache == null)
            {
                return;
            }
            rootCacheTL.Value = null;

            if (OfflineListenerExtendable != null)
            {
                OfflineListenerExtendable.RemoveOfflineListener(rootCache);
            }
            // Cut reference to persistence layer
            rootCache.Dispose();
        }
    }
}