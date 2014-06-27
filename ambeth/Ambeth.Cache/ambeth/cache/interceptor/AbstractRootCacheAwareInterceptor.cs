using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Util;
using System.Reflection;
using System.Threading;

namespace De.Osthus.Ambeth.Cache.Interceptor
{
    public abstract class AbstractRootCacheAwareInterceptor : IThreadLocalCleanupBean
    {
        protected static readonly MethodInfo clearMethod;

        static AbstractRootCacheAwareInterceptor()
        {
            clearMethod = ReflectUtil.GetDeclaredMethod(false, typeof(IWritableCache), typeof(void), "Clear");
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired(Optional = true)]
        public IOfflineListenerExtendable OfflineListenerExtendable { protected get; set; }

        [Autowired]
        public IServiceContext ServiceContext { protected get; set; }

        [Autowired]
        public ICacheRetriever StoredCacheRetriever { protected get; set; }

        protected IRootCache AcquireRootCache(bool privileged, ThreadLocal<RootCache> currentRootCacheTL)
        {
            return AcquireRootCache(privileged, currentRootCacheTL, StoredCacheRetriever, null, null);
        }

        protected IRootCache AcquireRootCache(bool privileged, ThreadLocal<RootCache> currentRootCacheTL, ICacheRetriever cacheRetriever, Lock readLock, Lock writeLock)
        {
            IBeanRuntime<RootCache> rootCacheBR = ServiceContext.RegisterAnonymousBean<RootCache>().PropertyValue("CacheRetriever", cacheRetriever);
            if (readLock != null)
            {
                rootCacheBR.PropertyValue("ReadLock", readLock);
            }
            if (writeLock != null)
            {
                rootCacheBR.PropertyValue("WriteLock", writeLock);
            }
            RootCache rootCache = PostProcessRootCacheConfiguration(rootCacheBR).PropertyValue("Privileged", privileged).Finish();

            if (OfflineListenerExtendable != null)
            {
                OfflineListenerExtendable.AddOfflineListener(rootCache);
            }
            currentRootCacheTL.Value = rootCache;
            return rootCache;
        }

        protected IBeanRuntime<RootCache> PostProcessRootCacheConfiguration(IBeanRuntime<RootCache> rootCacheBR)
        {
            // Do not inject EventQueue because caches without foreign interest will never receive async DCEs
            return rootCacheBR.IgnoreProperties("EventQueue").PropertyValue("WeakEntries", false);
        }

        protected void DisposeCurrentRootCache(ThreadLocal<RootCache> currentTL)
        {
            RootCache rootCache = currentTL.Value;
            currentTL.Value = null;
            if (rootCache == null)
            {
                return;
            }
            if (OfflineListenerExtendable != null)
            {
                OfflineListenerExtendable.RemoveOfflineListener(rootCache);
            }
            // Cut reference to persistence layer
            rootCache.Dispose();
        }

        public abstract void CleanupThreadLocal();
    }
}