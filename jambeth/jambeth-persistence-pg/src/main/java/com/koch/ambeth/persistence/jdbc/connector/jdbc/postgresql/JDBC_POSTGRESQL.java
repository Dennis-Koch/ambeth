package com.koch.ambeth.persistence.jdbc.connector.jdbc.postgresql;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connector.IConnector;
import com.koch.ambeth.persistence.pg.PostgresConnectionModule;
import com.koch.ambeth.persistence.pg.PostgresModule;

public class JDBC_POSTGRESQL implements IConnector
{
	@Override
	public void handleProperties(Properties props, String databaseProtocol)
	{
		props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, PostgresConnectionModule.class.getName());
	}

	@Override
	public void handleProd(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(PostgresModule.class);
	}
}
