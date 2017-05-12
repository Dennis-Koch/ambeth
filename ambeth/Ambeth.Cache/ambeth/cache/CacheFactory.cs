using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Config;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Threading;
#if SILVERLIGHT
using De.Osthus.Ambeth.Util;
#else
using System.Threading;
#endif
using System;

namespace De.Osthus.Ambeth.Cache
{
    public class CacheFactory : ICacheFactory
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
	    public IServiceContext BeanContext { protected get; set; }

        [Autowired]
        public IFirstLevelCacheExtendable FirstLevelCacheExtendable { protected get; set; }
		       
        [Property(MergeConfigurationConstants.SecurityActive, DefaultValue = "false")]
	    public bool SecurityActive { protected get; set; }

		protected readonly ThreadLocal<ICache> parentTL = new ThreadLocal<ICache>();

		public IDisposableCache WithParent(ICache parent, IResultingBackgroundWorkerDelegate<IDisposableCache> runnable)
		{
			ICache oldParent = parentTL.Value;
			parentTL.Value = parent;
			try
			{
				return runnable();
			}
			finally
			{
				parentTL.Value = oldParent;
			}
		}

	    public IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective, String name)
	    {
			if (!SecurityActive)
			{
				return CreatePrivileged(cacheFactoryDirective, name);
			}
            return CreateIntern(cacheFactoryDirective, false, false, null, name);
	    }

        public IDisposableCache CreatePrivileged(CacheFactoryDirective cacheFactoryDirective, String name)
        {
            return CreateIntern(cacheFactoryDirective, true, false, null, name);
        }

        public IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries, String name)
	    {
			if (!SecurityActive)
			{
				return CreatePrivileged(cacheFactoryDirective, foreignThreadAware, useWeakEntries, name);
			}
            return CreateIntern(cacheFactoryDirective, false, foreignThreadAware, useWeakEntries, name);
        }

        public IDisposableCache CreatePrivileged(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries, String name)
        {
            return CreateIntern(cacheFactoryDirective, true, foreignThreadAware, useWeakEntries, name);
        }

        protected IDisposableCache CreateIntern(CacheFactoryDirective cacheFactoryDirective, bool privileged, bool foreignThreadAware, bool? useWeakEntries, String name)
        {
		    IBeanRuntime<ChildCache> firstLevelCacheBC = BeanContext.RegisterBean<ChildCache>();
            if (!foreignThreadAware)
            {
                // Do not inject EventQueue because caches without foreign interest will never receive async DCEs
                firstLevelCacheBC.IgnoreProperties("EventQueue");
            }
            if (useWeakEntries.HasValue)
            {
                firstLevelCacheBC.PropertyValue("WeakEntries", useWeakEntries.Value);
            }
            if (name != null)
            {
                firstLevelCacheBC.PropertyValue("Name", name);
            }
            firstLevelCacheBC.PropertyValue("Privileged", privileged);
            ChildCache firstLevelCache = firstLevelCacheBC.Finish();
		    FirstLevelCacheExtendable.RegisterFirstLevelCache(firstLevelCache, cacheFactoryDirective, foreignThreadAware, name);
		    return firstLevelCache;
	    }
    }
}