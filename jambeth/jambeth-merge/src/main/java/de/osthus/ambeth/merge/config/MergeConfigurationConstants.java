package de.osthus.ambeth.merge.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;
import de.osthus.ambeth.merge.IEntityFactory;

@ConfigurationConstants
public final class MergeConfigurationConstants
{
	/**
	 * Defines which {@link IEntityFactory} should be used. Has to be a fully qualified class name. If not specified a default {@link IEntityFactory} will be
	 * used.
	 */
	public static final String EntityFactoryType = "ambeth.merge.entityfactory.type";

	@ConfigurationConstantDescription("TODO")
	public static final String FieldBasedMergeActive = "ambeth.merge.fieldbased.active";

	/**
	 * Defines whether the exact version is needed to avoid an OptimisticLockException during update of an entity or all versions equals or higher are accepted.
	 * Valid values are "true" and "false", default is "false".
	 */
	public static final String ExactVersionForOptimisticLockingRequired = "ambeth.merge.exact.version.required";

	/**
	 * Defines how to handle entity changes and the reload of entities to the cache. By default ("false") the version is not updated in changed entities,
	 * forcing the DataChangeEvent to refresh the cached object from the real data. If the version is updated the cache will not updated because the DCE will
	 * 'see' an already valid object - but it is NOT valid because it may not contain bi-directional information which can only be resolved by reloading the
	 * object from persistence layer. Valid values are "true" and "false", default is "false".
	 */
	public static final String AlwaysUpdateVersionInChangedEntities = "ambeth.merge.update.version.always";

	/**
	 * Allows to check the value-object mapping during parsing the mapping file. Valid values: "true" or "false". Default is "false".
	 */
	public static final String ValueObjectConfigValidationActive = "ambeth.mapping.config.validate.active";

	/**
	 * If security is enabled all security relevant tasks are checked whether they are allowed by the current user.
	 */
	public static final String SecurityActive = "ambeth.security.active";

	private MergeConfigurationConstants()
	{
		// Intended blank
	}
}
