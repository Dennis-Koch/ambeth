package com.koch.ambeth.bytecode.config;

import com.koch.ambeth.util.annotation.ConfigurationConstantDescription;
import com.koch.ambeth.util.annotation.ConfigurationConstants;

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
