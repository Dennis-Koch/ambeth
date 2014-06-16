package de.osthus.ambeth.testutil;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class TestUtilConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String DoCleanSchema = "testutil.persistence.docleanschema";

	@ConfigurationConstantDescription("TODO")
	public static final String DoExecuteStrict = "testutil.persistence.doexecutestrict";

	@ConfigurationConstantDescription("Flat to activate the logging of prepared statement parameters (only to use in development for debbugin!)")
	public static final String ParamLoggerActive = "testutil.persistence.paramlogger.active";

	private TestUtilConfigurationConstants()
	{
		// Intended blank
	}
}
