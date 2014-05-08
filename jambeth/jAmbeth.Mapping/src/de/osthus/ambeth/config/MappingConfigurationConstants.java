package de.osthus.ambeth.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class MappingConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String InitDirectRelationsInBusinessObjects = "mapping.businessobject.resolve.relations";

	private MappingConfigurationConstants()
	{
		// Intended blank
	}
}
