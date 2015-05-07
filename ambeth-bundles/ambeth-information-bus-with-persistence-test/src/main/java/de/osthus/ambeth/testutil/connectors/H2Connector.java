package de.osthus.ambeth.testutil.connectors;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.h2.H2ConnectionModule;
import de.osthus.ambeth.h2.H2ConnectionUrlProvider;
import de.osthus.ambeth.h2.H2Dialect;
import de.osthus.ambeth.h2.H2Module;
import de.osthus.ambeth.h2.H2TestDialect;
import de.osthus.ambeth.h2.H2TestModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;

public class H2Connector
{
	public static boolean handleProperties(Properties props, String databaseProtocol)
	{
		try
		{
			if (!H2Module.handlesDatabaseProtocol(databaseProtocol))
			{
				return false;
			}
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
		// props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionInterfaces, "org.h2.jdbc.JdbcConnection");
		props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, H2ConnectionModule.class.getName());
		return true;
	}

	public static boolean handleTestSetup(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		try
		{
			if (!H2Module.handlesDatabaseProtocol(databaseProtocol))
			{
				return false;
			}
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
		beanContextFactory.registerBean(H2ConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
		beanContextFactory.registerBean(H2Dialect.class).autowireable(IConnectionDialect.class);
		beanContextFactory.registerBean(H2TestDialect.class).autowireable(IConnectionTestDialect.class);
		return true;
	}

	public static boolean handleTest(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		try
		{
			if (!H2Module.handlesDatabaseProtocol(databaseProtocol))
			{
				return false;
			}
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
		beanContextFactory.registerBean(H2TestModule.class);
		return true;
	}
}
