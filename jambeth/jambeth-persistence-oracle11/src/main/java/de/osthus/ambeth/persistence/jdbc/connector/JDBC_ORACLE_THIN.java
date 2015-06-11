package de.osthus.ambeth.persistence.jdbc.connector;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.oracle.Oracle10gConnectionModule;
import de.osthus.ambeth.oracle.Oracle10gModule;
import de.osthus.ambeth.oracle.Oracle11gModule;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connector.IConnector;

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
