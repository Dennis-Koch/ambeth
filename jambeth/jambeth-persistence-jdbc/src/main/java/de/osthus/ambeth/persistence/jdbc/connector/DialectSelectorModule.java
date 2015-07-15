package de.osthus.ambeth.persistence.jdbc.connector;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;

@FrameworkModule
public class DialectSelectorModule implements IInitializingModule
{
	public static void fillProperties(Properties props)
	{
		String databaseProtocol = props.getString(PersistenceJdbcConfigurationConstants.DatabaseProtocol);
		if (databaseProtocol == null)
		{
			return;
		}
		IConnector connector = loadConnector(databaseProtocol);
		connector.handleProperties(props, databaseProtocol);
	}

	protected static IConnector loadConnector(String databaseProtocol)
	{
		String connectorName = databaseProtocol.toUpperCase().replace(':', '_');
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String fqConnectorName = DialectSelectorModule.class.getPackage().getName() + "." + connectorName;
		try
		{
			Class<?> connectorType = classLoader.loadClass(fqConnectorName);
			return (IConnector) connectorType.newInstance();
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
		IConnector connector = loadConnector(databaseProtocol);
		connector.handleProd(beanContextFactory, databaseProtocol);
	}
}
