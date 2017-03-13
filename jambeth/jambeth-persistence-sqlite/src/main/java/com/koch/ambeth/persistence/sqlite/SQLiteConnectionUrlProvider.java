package com.koch.ambeth.persistence.sqlite;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connection.AbstractConnectionUrlProvider;

public class SQLiteConnectionUrlProvider extends AbstractConnectionUrlProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected String getConnectionUrlIntern()
	{
		return resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseProtocol) + ":"//
				+ resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseName);
	}
}
