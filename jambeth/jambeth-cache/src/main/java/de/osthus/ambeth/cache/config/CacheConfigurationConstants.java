package de.osthus.ambeth.cache.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class CacheConfigurationConstants
{
	private CacheConfigurationConstants()
	{
		// intended blank
	}

	@ConfigurationConstantDescription("TODO")
	public static final String CacheServiceName = "cache.service.name";

	@ConfigurationConstantDescription("TODO")
	public static final String ServiceResultCacheActive = "cache.resultcache.active";

	@ConfigurationConstantDescription("TODO")
	public static final String ValueholderOnEmptyToOne = "cache.valueholder.onEmptyToOne";

	@ConfigurationConstantDescription("TODO")
	public static final String OverwriteToManyRelationsInChildCache = "cache.child.onupdate.overwritetomany";

	@ConfigurationConstantDescription("TODO")
	public static final String CacheLruThreshold = "cache.lru.threshold";

	@ConfigurationConstantDescription("TODO")
	public static final String FirstLevelCacheType = "cache.firstlevel.type";

	@ConfigurationConstantDescription("TODO")
	public static final String SecondLevelCacheActive = "cache.secondlevel.active";

	@ConfigurationConstantDescription("TODO")
	public static final String CacheReferenceCleanupInterval = "cache.weakref.cleanup.interval";

	@ConfigurationConstantDescription("TODO")
	public static final String QueryBehavior = "query.behavior";

	@ConfigurationConstantDescription("TODO")
	public static final String FirstLevelCacheWeakActive = "cache.firstlevel.weak.active";

	@ConfigurationConstantDescription("TODO")
	public static final String SecondLevelCacheWeakActive = "cache.secondlevel.weak.active";

	@ConfigurationConstantDescription("TODO")
	public static final String AsyncPropertyChangeActive = "cache.asyncpropertychange.active";

	@ConfigurationConstantDescription("TODO")
	public static final String FireOldPropertyValueActive = "cache.propertychange.fireoldvalue.active";
}
