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

	@ConfigurationConstantDescription("TODO")
	public static final String ExactVersionForOptimisticLockingRequired = "ambeth.merge.exact.version.required";

	@ConfigurationConstantDescription("TODO")
	public static final String AlwaysUpdateVersionInChangedEntities = "ambeth.merge.update.version.always";

	/**
	 * Allows to check the value-object mapping during parsing the mapping file. Valid values: "true" or "false". Default is "false".
	 */
	public static final String ValueObjectConfigValidationActive = "ambeth.mapping.config.validate.active";

	@ConfigurationConstantDescription("TODO")
	public static final String SecurityActive = "ambeth.security.active";

	private MergeConfigurationConstants()
	{
		// Intended blank
	}
}
