package com.koch.ambeth.query.config;

import com.koch.ambeth.util.annotation.ConfigurationConstantDescription;
import com.koch.ambeth.util.annotation.ConfigurationConstants;

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
