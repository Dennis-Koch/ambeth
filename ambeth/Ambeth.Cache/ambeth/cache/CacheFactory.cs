using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
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
       
	    public IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective)
	    {
		    return Create(cacheFactoryDirective, false, null);
	    }

	    public IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries)
	    {
		    IBeanRuntime<ChildCache> firstLevelCacheBC = BeanContext.RegisterAnonymousBean<ChildCache>();
            if (!foreignThreadAware)
            {
                // Do not inject EventQueue because caches without foreign interest will never receive async DCEs
                firstLevelCacheBC.IgnoreProperties("EventQueue");
            }
            if (useWeakEntries.HasValue)
            {
                firstLevelCacheBC.PropertyValue("WeakEntries", useWeakEntries.Value);
            }
            ChildCache firstLevelCache = firstLevelCacheBC.Finish();
		    FirstLevelCacheExtendable.RegisterFirstLevelCache(firstLevelCache, cacheFactoryDirective, foreignThreadAware);
		    return firstLevelCache;
	    }
    }
}