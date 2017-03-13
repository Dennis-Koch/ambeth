package com.koch.ambeth.persistence.jdbc.testconnector;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.testconnector.ITestConnector;

public class DialectSelectorSchemaModule implements IInitializingModule
{
	protected static ITestConnector loadTestConnector(String databaseProtocol)
	{
		String connectorName = databaseProtocol.toUpperCase().replace(':', '_');
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String fqConnectorName = DialectSelectorSchemaModule.class.getPackage().getName() + "." + connectorName;
		try
		{
			Class<?> connectorType = classLoader.loadClass(fqConnectorName);
			return (ITestConnector) connectorType.newInstance();
		}
		catch (Throwable e)
		{
			throw new IllegalStateException("Protocol not supported: '" + databaseProtocol + "'", e);
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseProtocol)
	protected String databaseProtocol;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		ITestConnector connector = loadTestConnector(databaseProtocol);
		connector.handleTestSetup(beanContextFactory, databaseProtocol);
	}
}
