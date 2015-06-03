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

	/**
	 * Not used in JAmbeth currently!
	 */
	@ConfigurationConstantDescription("TODO")
	public static final String CacheServiceName = "cache.service.name";

	/**
	 * Enables or disables the result cache. Valid values are "true" and "false", default is "false".
	 */
	public static final String ServiceResultCacheActive = "cache.resultcache.active";

	/**
	 * Not used in JAmbeth currently!
	 */
	@ConfigurationConstantDescription("TODO")
	public static final String ValueholderOnEmptyToOne = "cache.valueholder.onEmptyToOne";

	/**
	 * Defines whether during an update of the cache the to-many relations should be resetted. Valid values are "true" and "false", default is "true".
	 */
	public static final String OverwriteToManyRelationsInChildCache = "cache.child.onupdate.overwritetomany";

	/**
	 * Sets the threshold of the Least Recently Used entries. If activated the RootCache contains references to the last n entities in the a list so they are
	 * not garbage collected. Valid values are numbers > 0, default value is '0' which means the list is deactivated.
	 */
	public static final String CacheLruThreshold = "cache.lru.threshold";

	/**
	 * Defines the type of the first-level cache. Valid values are "DEFAULT", "PROTOTYPE", "SINGLETON" and "THREAD_LOCAL", default value is "DEFAULT".
	 */
	public static final String FirstLevelCacheType = "cache.firstlevel.type";

	/**
	 * Enables or disables the second level cache. Valid values are "true" and "false", default is "true".
	 */
	public static final String SecondLevelCacheActive = "cache.secondlevel.active";

	/**
	 * Not used in JAmbeth currently!
	 */
	@ConfigurationConstantDescription("TODO")
	public static final String CacheReferenceCleanupInterval = "cache.weakref.cleanup.interval";

	@ConfigurationConstantDescription("TODO")
	public static final String QueryBehavior = "query.behavior";

	/**
	 * Whether the first-level cache should be realized as a weak reference cache. If set to "true" the first-level cache will contain only weak reference, so
	 * the garbage collector can clear them if necessary. Valid values are "true" and "false", default is "true".
	 */
	public static final String FirstLevelCacheWeakActive = "cache.firstlevel.weak.active";

	/**
	 * Enables whether the second level cache should work with weak references to avoid memory problems. Valid values are "true" and "false", default is "true".
	 */
	public static final String SecondLevelCacheWeakActive = "cache.secondlevel.weak.active";

	/**
	 * Behavior of property change events, whether they are called asynchron in the gui thread ("true") or in the invoking thread ("false"). Valid values are
	 * "true" and "false", default is "false".
	 */
	public static final String AsyncPropertyChangeActive = "cache.asyncpropertychange.active";

	/**
	 * Defines whether the old value should be added to a PropertyChangeEvent which is fired by the cache. Valid values are "true" and "false", default is
	 * "false".
	 */
	public static final String FireOldPropertyValueActive = "cache.propertychange.fireoldvalue.active";
}
