using System;
using System.Threading;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Cache
{
    public class CacheProvider : IInitializingBean, IThreadLocalCleanupBean, ICacheProvider
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public virtual CacheType CacheType { protected get; set; }

        public virtual ICacheFactory CacheFactory { protected get; set; }

        protected volatile ICache singletonCache;

        public virtual IRootCache RootCache { protected get; set; }

        protected ThreadLocal<IDisposableCache> cacheTL;

        protected readonly Lock writeLock = new ReadWriteLock().WriteLock;

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(CacheFactory, "CacheFactory");
            ParamChecker.AssertNotNull(CacheType, "CacheType");
            ParamChecker.AssertNotNull(RootCache, "RootCache");

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
                        return CacheFactory.Create(CacheFactoryDirective.SubscribeTransactionalDCE, false, null);
                    }
                case CacheType.SINGLETON:
                    {
                        writeLock.Lock();
                        try
                        {
                            if (singletonCache == null)
                            {
                                singletonCache = CacheFactory.Create(CacheFactoryDirective.SubscribeTransactionalDCE, true, null);
                            }
                            return singletonCache;
                        }
                        finally
                        {
                            writeLock.Unlock();
                        }
                    }
                case CacheType.THREAD_LOCAL:
                    {
                        IDisposableCache cache = cacheTL.Value;
                        if (cache == null)
                        {
                            cache = CacheFactory.Create(CacheFactoryDirective.SubscribeTransactionalDCE, false, false);
                            cacheTL.Value = cache;
                        }
                        return cache;
                    }
                default:
                    throw new Exception("Not supported type: " + CacheType);
            }
        }
    }
}