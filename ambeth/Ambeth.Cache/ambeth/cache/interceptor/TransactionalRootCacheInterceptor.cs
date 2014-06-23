using De.Osthus.Ambeth.Cache.Rootcachevalue;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Log;
using System.Reflection;
using System;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Service;
using System.Threading;
using System.Collections.Generic;
#if SILVERLIGHT
using Castle.Core.Interceptor;
#else
using Castle.DynamicProxy;
#endif

namespace De.Osthus.Ambeth.Cache.Interceptor
{
    public class TransactionalRootCacheInterceptor : ThreadLocalRootCacheInterceptor, ITransactionalRootCache, ISecondLevelCacheManager
    {
        [LogInstance]
        public new ILogger Log { private get; set; }

        [Autowired]
        public RootCache CommittedRootCache { protected get; set; }

        protected readonly ThreadLocal<bool> transactionalRootCacheActiveTL = new ThreadLocal<bool>();
        
        public override void CleanupThreadLocal()
        {
            transactionalRootCacheActiveTL.Value = false;
            base.CleanupThreadLocal();
        }

        protected override IRootCache GetCurrentRootCacheIfValid()
        {
            IRootCache rootCache = base.GetCurrentRootCacheIfValid();
            if (rootCache == null && transactionalRootCacheActiveTL.Value)
			{
				// Lazy init of transactional rootcache
                if (SecurityActivation != null && !SecurityActivation.FilterActivated)
                {
                    rootCache = AcquireCurrentPrivilegedRootCache();
                }
                else
                {
                    rootCache = AcquireCurrentRootCache();
                }
    		}
            // If no thread-bound root cache is active (which implies that no transaction is currently active
            // return the unbound root cache (which reads uncommitted data)
            return rootCache != null ? rootCache : CommittedRootCache;
        }

        protected override IBeanRuntime<RootCache> PostProcessRootCacheConfiguration(IBeanRuntime<RootCache> rootCacheBR)
        {
            return base.PostProcessRootCacheConfiguration(rootCacheBR).IgnoreProperties("PrivilegeProvider", "SecurityActivation", "SecurityScopeProvider");
        }

        public IRootCache SelectSecondLevelCache()
        {
            return GetCurrentRootCacheIfValid();
        }

        public void AcquireTransactionalRootCache()
        {
            RootCache rootCache = rootCacheTL.Value;
            if (rootCache != null)
            {
                throw new Exception("Transactional root cache already acquired");
            }
            transactionalRootCacheActiveTL.Value = true;
        }

        public void DisposeTransactionalRootCache(bool success)
        {
            transactionalRootCacheActiveTL.Value = false;

            try
            {
                RootCache rootCache = rootCacheTL.Value;
                if (rootCache == null)
                {
                    // This may happen if an exception occurs while committing and therefore calling a rollback
                    return;
                }
                if (success)
                {
                    IList<RootCacheValue> content = new List<RootCacheValue>();

                    // Save information into second level cache for committed data
                    rootCache.GetContent(delegate(Type entityType, sbyte idIndex, Object id, Object value)
                        {
                            content.Add((RootCacheValue)value);
                        });
                    if (content.Count > 0)
                    {
                        rootCache.Clear();
                        CommittedRootCache.Put(content);
                    }
                }
            }
            finally
            {
                DisposeCurrentRootCache();
            }
        }
    }
}
