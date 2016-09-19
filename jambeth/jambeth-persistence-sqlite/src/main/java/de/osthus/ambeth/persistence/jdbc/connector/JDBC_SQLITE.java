package de.osthus.ambeth.persistence.jdbc.connector;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connector.IConnector;
import de.osthus.ambeth.sqlite.SQLiteConnectionModule;
import de.osthus.ambeth.sqlite.SQLiteModule;

public class JDBC_SQLITE implements IConnector
{
	@Override
	public void handleProperties(Properties props, String databaseProtocol)
	{
		props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, SQLiteConnectionModule.class.getName());
	}

	@Override
	public void handleProd(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(SQLiteModule.class);
	}
 }
