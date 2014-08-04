package de.osthus.ambeth.testutil;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.testutil.connectors.H2Connector;
import de.osthus.ambeth.testutil.connectors.OracleConnector;

public class DialectSelectorModule implements IInitializingModule
{
	public static void fillTestProperties(Properties props)
	{
		String databaseProtocol = props.getString(PersistenceJdbcConfigurationConstants.DatabaseProtocol);

		if (databaseProtocol == null)
		{
			return;
		}
		if (H2Connector.handleProperties(props, databaseProtocol))
		{
			return;
		}
		else if (OracleConnector.handleProperties(props, databaseProtocol))
		{
			return;
		}
		throw new IllegalStateException("Protocol not supported: '" + databaseProtocol + "'");
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseProtocol)
	protected String databaseProtocol;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (H2Connector.handleTest(beanContextFactory, databaseProtocol))
		{
			return;
		}
		else if (OracleConnector.handleTest(beanContextFactory, databaseProtocol))
		{
			return;
		}
		throw new IllegalStateException("Protocol not supported: '" + databaseProtocol + "'");
	}
}
