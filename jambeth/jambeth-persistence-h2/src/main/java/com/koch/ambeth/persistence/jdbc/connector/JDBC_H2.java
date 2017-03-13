package com.koch.ambeth.persistence.jdbc.connector;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.h2.H2ConnectionModule;
import com.koch.ambeth.persistence.h2.H2Module;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connector.IConnector;

public class JDBC_H2 implements IConnector
{
	@Override
	public void handleProperties(Properties props, String databaseProtocol)
	{
		// props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionInterfaces, "org.h2.jdbc.JdbcConnection");
		props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, H2ConnectionModule.class.getName());
	}

	@Override
	public void handleProd(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(H2Module.class);
	}
}
