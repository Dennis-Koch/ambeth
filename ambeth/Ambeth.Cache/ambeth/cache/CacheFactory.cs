using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Security.Config;
using De.Osthus.Ambeth.Util;

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

        [Autowired(Optional = true)]
        public ISecurityActivation SecurityActivation { protected get; set; }
       
        [Property(SecurityConfigurationConstants.SecurityActive, DefaultValue = "false")]
	    public bool SecurityActive { protected get; set; }

	    public IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective)
	    {
		    return CreateIntern(cacheFactoryDirective, false, false, null);
	    }

        public IDisposableCache CreatePrivileged(CacheFactoryDirective cacheFactoryDirective)
        {
            return CreateIntern(cacheFactoryDirective, true, false, null);
        }

	    public IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries)
	    {
            return CreateIntern(cacheFactoryDirective, false, foreignThreadAware, useWeakEntries);
        }

        public IDisposableCache CreatePrivileged(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries)
        {
            return CreateIntern(cacheFactoryDirective, true, foreignThreadAware, useWeakEntries);
        }

        protected IDisposableCache CreateIntern(CacheFactoryDirective cacheFactoryDirective, bool privileged, bool foreignThreadAware, bool? useWeakEntries)
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
            firstLevelCacheBC.PropertyValue("Privileged", privileged);
            ChildCache firstLevelCache = firstLevelCacheBC.Finish();
		    FirstLevelCacheExtendable.RegisterFirstLevelCache(firstLevelCache, cacheFactoryDirective, foreignThreadAware);
		    return firstLevelCache;
	    }
    }
}