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
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Security.Config;
using De.Osthus.Ambeth.Security;

namespace De.Osthus.Ambeth.Cache.Interceptor
{
    public class TransactionalRootCacheInterceptor : AbstractRootCacheAwareInterceptor, IInterceptor, ITransactionalRootCache,
            ISecondLevelCacheManager
    {
        [LogInstance]
        public new ILogger Log { private get; set; }

        [Autowired]
        public IRootCache CommittedRootCache { protected get; set; }

        [Autowired(Optional = true)]
        public ISecurityActivation SecurityActivation { protected get; set; }

        [Property(SecurityConfigurationConstants.SecurityActive, DefaultValue = "false")]
        public bool SecurityActive { protected get; set; }

        protected readonly ThreadLocal<RootCache> privilegedRootCacheTL = new ThreadLocal<RootCache>();

        protected readonly ThreadLocal<RootCache> rootCacheTL = new ThreadLocal<RootCache>();

        protected readonly ThreadLocal<Boolean> transactionalRootCacheActiveTL = new ThreadLocal<Boolean>();

        public override void CleanupThreadLocal()
        {
            transactionalRootCacheActiveTL.Value = false;
            DisposeCurrentRootCache(privilegedRootCacheTL);
            DisposeCurrentRootCache(rootCacheTL);
        }

        protected IRootCache GetCurrentRootCache(bool privileged)
        {
            IRootCache rootCache = privileged ? privilegedRootCacheTL.Value : rootCacheTL.Value;
            if (rootCache != null)
            {
                return rootCache;
            }
            if (!transactionalRootCacheActiveTL.Value)
            {
                // If no thread-bound root cache is active (which implies that no transaction is currently active
                // return the unbound root cache (which reads committed data)
                return CommittedRootCache;
            }
            // if we need a cache and security is active the privileged cache is a prerequisite in both cases
            IRootCache privilegedRootCache = privilegedRootCacheTL.Value;
            if (privilegedRootCache == null)
            {
                // here we know that the non-privileged one could not have existed before, so we simply create the privileged one
                privilegedRootCache = AcquireRootCache(privileged, privilegedRootCacheTL);
            }
            if (privileged)
            {
                // we need only the privilegedRootCache so we are finished
                return privilegedRootCache;
            }
            IRootCache nonPrivilegedRootCache = rootCacheTL.Value;
            if (nonPrivilegedRootCache == null)
            {
                // share the locks from the privileged rootCache
                nonPrivilegedRootCache = AcquireRootCache(privileged, rootCacheTL, (ICacheRetriever)privilegedRootCache, privilegedRootCache.ReadLock,
                        privilegedRootCache.WriteLock);
            }
            return nonPrivilegedRootCache;
        }

        public IRootCache SelectSecondLevelCache()
        {
            return GetCurrentRootCache(IsCurrentPrivileged());
        }

        protected bool IsCurrentPrivileged()
        {
            return !SecurityActive || !SecurityActivation.FilterActivated;
        }

        public void AcquireTransactionalRootCache()
        {
            if (privilegedRootCacheTL.Value != null || privilegedRootCacheTL.Value != null)
            {
                throw new Exception("Transactional root cache already acquired");
            }
            transactionalRootCacheActiveTL.Value = true;
        }

        public void DisposeTransactionalRootCache(bool success)
        {
            transactionalRootCacheActiveTL.Value = false;

            DisposeCurrentRootCache(rootCacheTL);

            IRootCache rootCache = privilegedRootCacheTL.Value;
            if (rootCache == null)
            {
                DisposeCurrentRootCache(privilegedRootCacheTL);
                // This may happen if an exception occurs while committing and therefore calling a rollback
                return;
            }
            try
            {
                if (success)
                {
                    List<RootCacheValue> content = new List<RootCacheValue>();

                    // Save information into second level cache for committed data
                    rootCache.GetContent(delegate(Type entityType, sbyte idIndex, Object id, Object value)
                        {
                            content.Add((RootCacheValue)value);
                        }
                    );
                    if (content.Count > 0)
                    {
                        rootCache.Clear();
                        CommittedRootCache.Put(content);
                    }
                }
            }
            finally
            {
                DisposeCurrentRootCache(privilegedRootCacheTL);
            }
        }

        public void Intercept(IInvocation invocation)
        {
            if (clearMethod.Equals(invocation.Method))
            {
                IRootCache rootCache = privilegedRootCacheTL.Value;
                if (rootCache != null)
                {
                    rootCache.Clear();
                }
                rootCache = rootCacheTL.Value;
                if (rootCache != null)
                {
                    rootCache.Clear();
                }
                return;
            }
            ICacheIntern requestingCache = null;
            foreach (Object arg in invocation.Arguments)
            {
                if (arg is ICacheIntern)
                {
                    requestingCache = (ICacheIntern)arg;
                    break;
                }
            }
            bool privileged;
            if (requestingCache != null)
            {
                privileged = requestingCache.Privileged;
            }
            else
            {
                privileged = IsCurrentPrivileged();
            }
            IRootCache rootCache2 = GetCurrentRootCache(privileged);
            invocation.ReturnValue = invocation.Method.Invoke(rootCache2, invocation.Arguments);
        }
    }
}
