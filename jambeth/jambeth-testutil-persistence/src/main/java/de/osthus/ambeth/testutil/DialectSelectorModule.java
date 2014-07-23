package de.osthus.ambeth.testutil;

import oracle.jdbc.OracleConnection;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.h2.H2ConnectionModule;
import de.osthus.ambeth.h2.H2Module;
import de.osthus.ambeth.h2.H2TestModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.oracle.Oracle10gConnectionModule;
import de.osthus.ambeth.oracle.Oracle10gModule;
import de.osthus.ambeth.oracle.Oracle10gTestModule;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;

public class DialectSelectorModule implements IInitializingModule
{
	public static void fillTestProperties(Properties props)
	{
		String databaseProtocol = props.getString(PersistenceJdbcConfigurationConstants.DatabaseProtocol);

		if (H2Module.handlesDatabaseProtocol(databaseProtocol))
		{
			// props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionInterfaces, "org.h2.jdbc.JdbcConnection");
			props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, H2ConnectionModule.class.getName());
		}
		else if (Oracle10gModule.handlesDatabaseProtocol(databaseProtocol))
		{
			props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionInterfaces, OracleConnection.class.getName());
			props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, Oracle10gConnectionModule.class.getName());
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
		if (H2Module.handlesDatabaseProtocol(databaseProtocol))
		{
			beanContextFactory.registerAnonymousBean(H2TestModule.class);
		}
		else if (Oracle10gModule.handlesDatabaseProtocol(databaseProtocol))
		{
			beanContextFactory.registerAnonymousBean(Oracle10gTestModule.class);
		}
		else
		{
			throw new IllegalStateException("Protocol not supported: '" + databaseProtocol + "'");
		}
	}
}
