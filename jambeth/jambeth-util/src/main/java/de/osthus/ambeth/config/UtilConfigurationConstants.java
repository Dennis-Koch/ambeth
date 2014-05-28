package de.osthus.ambeth.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class UtilConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String BootstrapPropertyFile = "property.file";

	@ConfigurationConstantDescription("TODO")
	public static final String DebugMode = "ambeth.debug.active";

	private UtilConfigurationConstants()
	{
		// Intended blank
	}
}
