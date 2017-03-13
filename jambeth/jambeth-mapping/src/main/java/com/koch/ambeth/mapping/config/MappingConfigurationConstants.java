package com.koch.ambeth.mapping.config;

import com.koch.ambeth.util.annotation.ConfigurationConstantDescription;
import com.koch.ambeth.util.annotation.ConfigurationConstants;

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
