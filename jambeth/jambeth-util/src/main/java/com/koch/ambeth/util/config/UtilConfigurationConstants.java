package com.koch.ambeth.util.config;

import com.koch.ambeth.util.annotation.ConfigurationConstantDescription;
import com.koch.ambeth.util.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class UtilConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String BootstrapPropertyFile = "property.file";

	/**
	 * Allows to run Ambeth in debug mode. This e.g. enables more exception to ease debugging. Valid values: "true" or "false". Default is "false".
	 */
	public static final String DebugMode = "ambeth.debug.active";

	/**
	 * Allows to define a name which is attached to e.g. log statements or db users during tests to distinguish them from normal/productive use.
	 */
	public static final String ForkName = "ambeth.test.forkname";

	private UtilConfigurationConstants()
	{
		// Intended blank
	}
}
