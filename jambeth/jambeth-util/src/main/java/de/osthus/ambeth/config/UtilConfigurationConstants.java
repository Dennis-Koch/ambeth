package de.osthus.ambeth.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class UtilConfigurationConstants {
	@ConfigurationConstantDescription("TODO")
	public static final String BootstrapPropertyFile = "property.file";

	/**
	 * Allows to run Ambeth in debug mode. This e.g. enables more exception to
	 * ease debugging. Valid values: "true" or "false". Default is "false".
	 */
	public static final String DebugMode = "ambeth.debug.active";

	@ConfigurationConstantDescription("TODO")
	public static final String ForkName = "ambeth.test.forkname";

	private UtilConfigurationConstants() {
		// Intended blank
	}
}
