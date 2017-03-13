package com.koch.ambeth.persistence.h2;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connection.AbstractConnectionUrlProvider;

public class H2ConnectionUrlProvider extends AbstractConnectionUrlProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected String getConnectionUrlIntern()
	{
		String protocol = resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseProtocol);
		if (protocol.equals("jdbc:h2:mem"))
		{
			return protocol + ":" + resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseName);
		}
		return resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseProtocol) + ":@"//
				+ resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseHost) + ":"//
				+ resolveProperty(PersistenceJdbcConfigurationConstants.DatabasePort) + ":"//
				+ resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseName);
	}
}
