package com.koch.ambeth.persistence.maria;

import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connection.AbstractConnectionUrlProvider;

public class MariaConnectionUrlProvider extends AbstractConnectionUrlProvider {
	@Override
	protected String getConnectionUrlIntern() {
		return resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseProtocol) + "://"//
				+ resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseHost) + ":"//
				+ resolveProperty(PersistenceJdbcConfigurationConstants.DatabasePort);
	}
}
