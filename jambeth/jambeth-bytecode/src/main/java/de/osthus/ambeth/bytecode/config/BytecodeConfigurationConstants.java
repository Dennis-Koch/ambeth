package de.osthus.ambeth.bytecode.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class BytecodeConfigurationConstants
{
	private BytecodeConfigurationConstants()
	{
		// intended blank
	}

	/**
	 * If specified all bytecode enhancements will be written to this directory for debugging purpose
	 */
	@ConfigurationConstantDescription("TODO")
	public static final String EnhancementTraceDirectory = "ambeth.bytecode.tracedir";
}
