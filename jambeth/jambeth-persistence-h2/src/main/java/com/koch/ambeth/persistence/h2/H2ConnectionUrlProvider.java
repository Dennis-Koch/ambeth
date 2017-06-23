package com.koch.ambeth.persistence.h2;

import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connection.AbstractConnectionUrlProvider;

public class H2ConnectionUrlProvider extends AbstractConnectionUrlProvider {
	@Override
	protected String getConnectionUrlIntern() {
		String protocol = resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseProtocol);
		if ("jdbc:h2:mem".equals(protocol)) {
			return protocol + ":" + resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseName);
		}
		return resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseProtocol) + ":@"//
				+ resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseHost) + ":"//
				+ resolveProperty(PersistenceJdbcConfigurationConstants.DatabasePort) + ":"//
				+ resolveProperty(PersistenceJdbcConfigurationConstants.DatabaseName);
	}
}
