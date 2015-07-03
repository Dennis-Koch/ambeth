package de.osthus.ambeth.config;

import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class DataChangePersistenceConfigurationConstants
{
	/**
	 * The time in ms which datachange events should not be removed. After the time has expired the events are removed from the database. Valid values are all
	 * numbers > 0 and -1, whereas -1 means the events are stored forever.
	 */
	public static final String KeepEventsForMillis = "datachange.persistence.keepevents.millis";

	private DataChangePersistenceConfigurationConstants()
	{
		// Intended blank
	}
}
