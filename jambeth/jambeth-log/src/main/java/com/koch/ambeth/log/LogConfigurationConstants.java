package com.koch.ambeth.log;

import com.koch.ambeth.util.annotation.ConfigurationConstants;

@ConfigurationConstants
public class LogConfigurationConstants
{
	/**
	 * The path to the file to which Ambeth should write the log statements. No default value, if not set Ambeth will not log to file.
	 */
	public static final String LogFile = "ambeth.log.file";

	private LogConfigurationConstants()
	{
		// Intended blank
	}
}
