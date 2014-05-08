using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Cache.Config
{
    public class CacheConfigurationConstants
    {
        public const String CacheServiceName = "cache.service.name";

        public const String CacheServiceRegistryActive = "cache.serviceregistry.active";

    	public const String EntityEnhancerActive = "cache.entityenhancer.active";

        public const String ServiceResultCacheActive = "cache.resultcache.active";

        public const String ValueholderOnEmptyToOne = "cache.valueholder.onEmptyToOne";

        public const String OverwriteToManyRelationsInChildCache = "cache.child.onupdate.overwritetomany";

        public const String UpdateChildCache = "cache.child.doUpdate";

        public const String CacheServiceBeanActive = "cache.service.active";

        public const String CacheLruThreshold = "cache.lru.threshold";

        public const String FirstLevelCacheType = "cache.firstlevel.type";

        public const String SecondLevelCacheActive = "cache.secondlevel.active";

        public const String CacheReferenceCleanupInterval = "cache.weakref.cleanup.interval";

        public const String ToOneDefaultCascadeLoadMode = "cache.cascadeload.toone";

        public const String ToManyDefaultCascadeLoadMode = "cache.cascadeload.tomany";

        public const String FirstLevelCacheWeakActive = "cache.firstlevel.weak.active";

        public const String SecondLevelCacheWeakActive = "cache.secondlevel.weak.active";

        public const String AsyncPropertyChangeActive = "cache.asyncpropertychange.active";
    }
}