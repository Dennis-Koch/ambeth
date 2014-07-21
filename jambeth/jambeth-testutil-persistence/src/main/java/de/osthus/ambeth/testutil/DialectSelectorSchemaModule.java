package de.osthus.ambeth.testutil;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.h2.H2Dialect;
import de.osthus.ambeth.h2.H2Module;
import de.osthus.ambeth.h2.H2TestDialect;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.oracle.Oracle10gDialect;
import de.osthus.ambeth.oracle.Oracle10gModule;
import de.osthus.ambeth.oracle.Oracle10gTestDialect;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;

public class DialectSelectorSchemaModule implements IInitializingModule
{
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
			beanContextFactory.registerAnonymousBean(H2Dialect.class).autowireable(IConnectionDialect.class);
			beanContextFactory.registerAnonymousBean(H2TestDialect.class).autowireable(IConnectionTestDialect.class);
		}
		else if (Oracle10gModule.handlesDatabaseProtocol(databaseProtocol))
		{
			beanContextFactory.registerAnonymousBean(Oracle10gDialect.class).autowireable(IConnectionDialect.class);
			beanContextFactory.registerAnonymousBean(Oracle10gTestDialect.class).autowireable(IConnectionTestDialect.class);
		}
		else
		{
			throw new IllegalStateException("Protocol not supported: '" + databaseProtocol + "'");
		}
	}
}
