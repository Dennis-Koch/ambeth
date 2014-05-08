package de.osthus.ambeth.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class DataChangePersistenceConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String KeepEventsForMillis = "datachange.persistence.keepevents.millis";

	private DataChangePersistenceConfigurationConstants()
	{
		// Intended blank
	}
}
