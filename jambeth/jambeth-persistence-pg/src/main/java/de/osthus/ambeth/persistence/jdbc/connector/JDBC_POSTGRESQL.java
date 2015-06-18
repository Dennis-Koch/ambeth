package de.osthus.ambeth.persistence.jdbc.connector;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connector.IConnector;
import de.osthus.ambeth.pg.PostgresConnectionModule;
import de.osthus.ambeth.pg.PostgresModule;

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
