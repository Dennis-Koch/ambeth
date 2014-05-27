package de.osthus.ambeth.query.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class QueryConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String PagingPrefetchBehavior = "query.paging.prefetch";

	private QueryConfigurationConstants()
	{
		// Intended blank
	}
}
