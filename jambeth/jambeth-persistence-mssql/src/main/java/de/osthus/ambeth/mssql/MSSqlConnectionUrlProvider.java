package de.osthus.ambeth.mssql;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.AbstractConnectionUrlProvider;

public class MSSqlConnectionUrlProvider extends AbstractConnectionUrlProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected String getConnectionUrlIntern()
	{
		return resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseProtocol) + "://"//
				+ resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseHost) + ":"//
				+ resolveProperty(PersistenceJdbcConfigurationConstants.DatabasePort);
	}
}
