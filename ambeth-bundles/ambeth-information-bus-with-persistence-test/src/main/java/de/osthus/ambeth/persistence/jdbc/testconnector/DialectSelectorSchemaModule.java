package de.osthus.ambeth.persistence.jdbc.testconnector;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;

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
