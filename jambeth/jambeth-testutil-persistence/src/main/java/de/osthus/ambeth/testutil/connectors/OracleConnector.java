package de.osthus.ambeth.testutil.connectors;

import oracle.jdbc.OracleConnection;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.oracle.Oracle10gConnectionModule;
import de.osthus.ambeth.oracle.Oracle10gDialect;
import de.osthus.ambeth.oracle.Oracle10gModule;
import de.osthus.ambeth.oracle.Oracle10gTestDialect;
import de.osthus.ambeth.oracle.Oracle10gTestModule;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;

public class OracleConnector
{
	public static boolean handleProperties(Properties props, String databaseProtocol)
	{
		try
		{
			if (!Oracle10gModule.handlesDatabaseProtocol(databaseProtocol))
			{
				return false;
			}
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
		props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionInterfaces, OracleConnection.class.getName());
		props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, Oracle10gConnectionModule.class.getName());
		return true;
	}

	public static boolean handleTestSetup(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		try
		{
			if (!Oracle10gModule.handlesDatabaseProtocol(databaseProtocol))
			{
				return false;
			}
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
		beanContextFactory.registerAnonymousBean(Oracle10gDialect.class).autowireable(IConnectionDialect.class);
		beanContextFactory.registerAnonymousBean(Oracle10gTestDialect.class).autowireable(IConnectionTestDialect.class);
		return true;
	}

	public static boolean handleTest(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		try
		{
			if (!Oracle10gModule.handlesDatabaseProtocol(databaseProtocol))
			{
				return false;
			}
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
		beanContextFactory.registerAnonymousBean(Oracle10gTestModule.class);
		return true;
	}
}
