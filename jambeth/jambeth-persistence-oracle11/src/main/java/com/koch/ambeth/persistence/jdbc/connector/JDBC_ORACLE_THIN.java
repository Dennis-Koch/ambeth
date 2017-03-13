package com.koch.ambeth.persistence.jdbc.connector;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connector.IConnector;
import com.koch.ambeth.persistence.oracle.Oracle10gConnectionModule;
import com.koch.ambeth.persistence.oracle.Oracle10gModule;
import com.koch.ambeth.persistence.oracle.Oracle11gModule;

public class JDBC_ORACLE_THIN implements IConnector
{
	@Override
	public void handleProperties(Properties props, String databaseProtocol)
	{
		props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, Oracle10gConnectionModule.class.getName());
	}

	@Override
	public void handleProd(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(Oracle10gModule.class);
		beanContextFactory.registerBean(Oracle11gModule.class);
	}
}
