package de.osthus.ambeth.testutil.connectors;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.mssql.MSSqlConnectionModule;
import de.osthus.ambeth.mssql.MSSqlConnectionUrlProvider;
import de.osthus.ambeth.mssql.MSSqlDialect;
import de.osthus.ambeth.mssql.MSSqlModule;
import de.osthus.ambeth.mssql.MSSqlTestDialect;
import de.osthus.ambeth.mssql.MSSqlTestModule;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;

public class MSSqlConnector
{
	public static boolean handleProperties(Properties props, String databaseProtocol)
	{
		try
		{
			if (!MSSqlModule.handlesDatabaseProtocol(databaseProtocol))
			{
				return false;
			}
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
		props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, MSSqlConnectionModule.class.getName());
		return true;
	}

	public static boolean handleTestSetup(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		try
		{
			if (!MSSqlModule.handlesDatabaseProtocol(databaseProtocol))
			{
				return false;
			}
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
		beanContextFactory.registerBean(MSSqlConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
		beanContextFactory.registerBean(MSSqlDialect.class).autowireable(IConnectionDialect.class);
		beanContextFactory.registerBean(MSSqlTestDialect.class).autowireable(IConnectionTestDialect.class);
		return true;
	}

	public static boolean handleTest(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		try
		{
			if (!MSSqlModule.handlesDatabaseProtocol(databaseProtocol))
			{
				return false;
			}
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
		beanContextFactory.registerBean(MSSqlTestModule.class);
		return true;
	}
}
