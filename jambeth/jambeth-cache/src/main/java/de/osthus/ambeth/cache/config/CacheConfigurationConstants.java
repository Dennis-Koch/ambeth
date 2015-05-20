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

	/**
	 * Defines whether during an update of the cache the to-many relations should be resetted. Valid values are "true" and "false", default is "true".
	 */
	public static final String OverwriteToManyRelationsInChildCache = "cache.child.onupdate.overwritetomany";

	@ConfigurationConstantDescription("TODO")
	public static final String CacheLruThreshold = "cache.lru.threshold";

	/**
	 * Defines the type of the first-level cache. Valid values are "DEFAULT", "PROTOTYPE", "SINGLETON" and "THREAD_LOCAL", default value is "DEFAULT".
	 */
	public static final String FirstLevelCacheType = "cache.firstlevel.type";

	@ConfigurationConstantDescription("TODO")
	public static final String SecondLevelCacheActive = "cache.secondlevel.active";

	@ConfigurationConstantDescription("TODO")
	public static final String CacheReferenceCleanupInterval = "cache.weakref.cleanup.interval";

	@ConfigurationConstantDescription("TODO")
	public static final String QueryBehavior = "query.behavior";

	/**
	 * Whether the first-level cache should be realized as a weak reference cache. If set to "true" the first-level cache will contain only weak reference, so
	 * the garbage collector can clear them if necessary. Valid values are "true" and "false", default is "true".
	 */
	public static final String FirstLevelCacheWeakActive = "cache.firstlevel.weak.active";

	@ConfigurationConstantDescription("TODO")
	public static final String SecondLevelCacheWeakActive = "cache.secondlevel.weak.active";

	/**
	 * Behavior of property change events, whether they are called asynchron in the gui thread ("true") or in the invoking thread ("false"). Valid values are
	 * "true" and "false", default is "false".
	 */
	public static final String AsyncPropertyChangeActive = "cache.asyncpropertychange.active";

	@ConfigurationConstantDescription("TODO")
	public static final String FireOldPropertyValueActive = "cache.propertychange.fireoldvalue.active";
}
