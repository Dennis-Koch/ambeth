package com.koch.ambeth.persistence.jdbc.connector.jdbc.sqlite;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connector.IConnector;
import com.koch.ambeth.persistence.sqlite.SQLiteConnectionModule;
import com.koch.ambeth.persistence.sqlite.SQLiteModule;

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
