package de.osthus.ambeth.merge.config;

import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class MergeServerConfigurationConstants
{
	/**
	 * If true datachanges for deletes are generated for every ID (PK & AKs). Valid values are "true" and "false", default is "false".
	 */
	public static final String DeleteDataChangesByAlternateIds = "ambeth.merge.datachanges.delete.alternateids";

	private MergeServerConfigurationConstants()
	{
		// Intended blank
	}
}
