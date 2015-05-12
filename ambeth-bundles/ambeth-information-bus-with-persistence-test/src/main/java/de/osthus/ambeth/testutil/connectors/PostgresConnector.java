package de.osthus.ambeth.testutil.connectors;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;
import de.osthus.ambeth.pg.PostgresConnectionModule;
import de.osthus.ambeth.pg.PostgresConnectionUrlProvider;
import de.osthus.ambeth.pg.PostgresDialect;
import de.osthus.ambeth.pg.PostgresModule;
import de.osthus.ambeth.pg.PostgresTestDialect;
import de.osthus.ambeth.pg.PostgresTestModule;

public class PostgresConnector
{
	public static boolean handleProperties(Properties props, String databaseProtocol)
	{
		try
		{
			if (!PostgresModule.handlesDatabaseProtocol(databaseProtocol))
			{
				return false;
			}
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
		props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, PostgresConnectionModule.class.getName());
		return true;
	}

	public static boolean handleTestSetup(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		try
		{
			if (!PostgresModule.handlesDatabaseProtocol(databaseProtocol))
			{
				return false;
			}
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
		beanContextFactory.registerBean(PostgresConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
		beanContextFactory.registerBean(PostgresDialect.class).autowireable(IConnectionDialect.class);
		beanContextFactory.registerBean(PostgresTestDialect.class).autowireable(IConnectionTestDialect.class);
		return true;
	}

	public static boolean handleTest(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		try
		{
			if (!PostgresModule.handlesDatabaseProtocol(databaseProtocol))
			{
				return false;
			}
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
		beanContextFactory.registerBean(PostgresTestModule.class);
		return true;
	}
}
