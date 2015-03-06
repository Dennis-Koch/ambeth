package de.osthus.ambeth.pg;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.IConnectionExtension;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.sql.IPrimaryKeyProvider;

public class PostgresModule implements IInitializingModule
{
	public static boolean handlesDatabaseProtocol(String databaseProtocol)
	{
		return databaseProtocol.toLowerCase().startsWith("jdbc:postgresql");
	}

	@LogInstance
	private ILogger log;

	@Property(name = PersistenceConfigurationConstants.ExternalTransactionManager, defaultValue = "false")
	protected boolean externalTransactionManager;

	@Property(name = PersistenceJdbcConfigurationConstants.IntegratedConnectionPool, defaultValue = "true")
	protected boolean integratedConnectionPool;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseBehaviourStrict, defaultValue = "false")
	protected boolean databaseBehaviourStrict;

	@Property(name = PersistenceConfigurationConstants.DatabasePoolPassivate, defaultValue = "false")
	protected boolean databasePassivate;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(PostgresConnectionExtension.class).autowireable(IConnectionExtension.class);
		beanContextFactory.registerBean(PostgresDialect.class).autowireable(IConnectionDialect.class);
		beanContextFactory.registerBean(PostgresSequencePrimaryKeyProvider.class).autowireable(IPrimaryKeyProvider.class);
	}
}
