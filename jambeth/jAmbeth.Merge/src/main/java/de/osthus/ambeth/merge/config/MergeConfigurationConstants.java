package de.osthus.ambeth.merge.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class MergeConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String EntityFactoryType = "ambeth.merge.entityfactory.type";

	@ConfigurationConstantDescription("If true datachanges for deletes are generated for every ID (PK & AKs), default=false")
	public static final String DeleteDataChangesByAlternateIds = "ambeth.merge.datachanges.delete.alternateids";

	@ConfigurationConstantDescription("TODO")
	public static final String FieldBasedMergeActive = "ambeth.merge.fieldbased.active";

	@ConfigurationConstantDescription("TODO")
	public static final String ExactVersionForOptimisticLockingRequired = "ambeth.merge.exact.version.required";

	@ConfigurationConstantDescription("TODO")
	public static final String AlwaysUpdateVersionInChangedEntities = "ambeth.merge.update.version.always";

	@ConfigurationConstantDescription("TODO")
	public static final String ValueObjectConfigValidationActive = "ambeth.mapping.config.validate.active";

	private MergeConfigurationConstants()
	{
		// Intended blank
	}
}
