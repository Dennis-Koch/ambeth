package de.osthus.ambeth.persistence.jdbc.connector;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.mssql.MSSqlConnectionModule;
import de.osthus.ambeth.mssql.MSSqlModule;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connector.IConnector;

public class JDBC_SQLSERVER implements IConnector
{
	@Override
	public void handleProperties(Properties props, String databaseProtocol)
	{
		props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, MSSqlConnectionModule.class.getName());
	}

	@Override
	public void handleProd(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(MSSqlModule.class);
	}
}
