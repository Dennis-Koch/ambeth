package de.osthus.ambeth.h2;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.AbstractConnectionUrlProvider;

public class H2ConnectionUrlProvider extends AbstractConnectionUrlProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected String getConnectionUrlIntern()
	{
		return resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseProtocol) + ":@"//
				+ resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseHost) + ":"//
				+ resolveProperty(PersistenceJdbcConfigurationConstants.DatabasePort) + ":"//
				+ resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseName);
	}
}
