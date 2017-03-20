package com.koch.ambeth.persistence.jdbc.connector.jdbc.mariadb;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connector.IConnector;
import com.koch.ambeth.persistence.maria.MariaConnectionModule;
import com.koch.ambeth.persistence.maria.MariaModule;

public class JDBC_MARIADB implements IConnector
{
	@Override
	public void handleProperties(Properties props, String databaseProtocol)
	{
		props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, MariaConnectionModule.class.getName());
	}

	@Override
	public void handleProd(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(MariaModule.class);
	}
}
