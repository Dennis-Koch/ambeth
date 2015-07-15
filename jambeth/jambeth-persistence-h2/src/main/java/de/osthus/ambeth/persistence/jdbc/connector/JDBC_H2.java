package de.osthus.ambeth.persistence.jdbc.connector;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.h2.H2ConnectionModule;
import de.osthus.ambeth.h2.H2Module;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connector.IConnector;

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
