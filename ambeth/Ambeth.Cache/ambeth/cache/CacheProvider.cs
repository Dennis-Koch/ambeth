using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Config;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Util;
using System;
using System.Threading;

namespace De.Osthus.Ambeth.Cache
{
    public class CacheProvider : IInitializingBean, IThreadLocalCleanupBean, ICacheProvider
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public ICacheFactory CacheFactory { protected get; set; }

        [Autowired]
        public IRootCache RootCache { protected get; set; }

        [Autowired(Optional = true)]
        public ISecurityActivation SecurityActivation { protected get; set; }

        [Property(MergeConfigurationConstants.SecurityActive, DefaultValue = "false")]
        public bool SecurityActive { protected get; set; }

        [Property]
        public CacheType CacheType { protected get; set; }

        protected volatile ICache singletonCache;

        protected volatile ICache privilegedSingletonCache;

        [Forkable]
        protected ThreadLocal<IDisposableCache> cacheTL;

        [Forkable]
        protected ThreadLocal<IDisposableCache> privilegedCacheTL;

        protected readonly Lock writeLock = new ReadWriteLock().WriteLock;

        public virtual void AfterPropertiesSet()
        {
            switch (CacheType)
            {
                case CacheType.PROTOTYPE:
                    {
                        break;
                    }
                case CacheType.SINGLETON:
                    {
                        break;
                    }
                case CacheType.THREAD_LOCAL:
                    {
                        cacheTL = new ThreadLocal<IDisposableCache>();
                        if (SecurityActivation != null)
                        {
                            privilegedCacheTL = new ThreadLocal<IDisposableCache>();
                        }
                        break;
                    }
                default:
                    throw new Exception("Not supported type: " + CacheType);
            }
        }

        public virtual void CleanupThreadLocal()
        {
            if (cacheTL != null)
            {
                IDisposableCache cache = cacheTL.Value;
                if (cache != null)
                {
                    cacheTL.Value = null;
                    cache.Dispose();
                }
            }
            if (privilegedCacheTL != null)
            {
                IDisposableCache cache = privilegedCacheTL.Value;
                if (cache != null)
                {
                    privilegedCacheTL.Value = null;
                    cache.Dispose();
                }
            }
        }

        public bool IsNewInstanceOnCall
        {
            get
            {
                switch (CacheType)
                {
                    case CacheType.PROTOTYPE:
                        {
                            return true;
                        }
                    case CacheType.SINGLETON:
                    case CacheType.THREAD_LOCAL:
                        {
                            return false;
                        }
                    default:
                        throw new Exception("Not supported type: " + CacheType);
                }
            }
        }

        public ICache GetCurrentCache()
        {
            switch (CacheType)
            {
                case CacheType.PROTOTYPE:
                    {
                        if (!SecurityActive || !SecurityActivation.FilterActivated)
                        {
                            return CacheFactory.CreatePrivileged(CacheFactoryDirective.SubscribeTransactionalDCE, false, null, "CacheProvider.PROTOTYPE");
                        }
                        return CacheFactory.Create(CacheFactoryDirective.SubscribeTransactionalDCE, false, null, "CacheProvider.PROTOTYPE");
                    }
                case CacheType.SINGLETON:
                    {
                        writeLock.Lock();
                        try
                        {
                            if (!SecurityActive || !SecurityActivation.FilterActivated)
                            {
                                if (privilegedSingletonCache == null)
                                {
                                    privilegedSingletonCache = CacheFactory.CreatePrivileged(CacheFactoryDirective.SubscribeTransactionalDCE, true, null, "CacheProvider.SINGLETON");
                                }
                                return privilegedSingletonCache;
                            }
                            else
                            {
                                if (singletonCache == null)
                                {
                                    singletonCache = CacheFactory.Create(CacheFactoryDirective.SubscribeTransactionalDCE, true, null, "CacheProvider.SINGLETON");
                                }
                                return singletonCache;
                            }
                        }
                        finally
                        {
                            writeLock.Unlock();
                        }
                    }
                case CacheType.THREAD_LOCAL:
                    {
                        if (!SecurityActive || !SecurityActivation.FilterActivated)
                        {
                            IDisposableCache cache = privilegedCacheTL.Value;
                            if (cache == null)
                            {
                                cache = CacheFactory.CreatePrivileged(CacheFactoryDirective.SubscribeTransactionalDCE, false, false, "CacheProvider.THREAD_LOCAL");
                                privilegedCacheTL.Value = cache;
                            }
                            return cache;
                        }
                        else
                        {
                            IDisposableCache cache = cacheTL.Value;
                            if (cache == null)
                            {
                                cache = CacheFactory.Create(CacheFactoryDirective.SubscribeTransactionalDCE, false, false, "CacheProvider.THREAD_LOCAL");
                                cacheTL.Value = cache;
                            }
                            return cache;
                        }
                    }
                default:
                    throw new Exception("Not supported type: " + CacheType);
            }
        }
    }
}