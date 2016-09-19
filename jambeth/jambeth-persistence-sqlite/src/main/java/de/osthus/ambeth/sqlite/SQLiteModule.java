package de.osthus.ambeth.sqlite;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.IExtendedConnectionDialect;
import de.osthus.ambeth.persistence.IPrimaryKeyProvider;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.IConnectionExtension;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;

public class SQLiteModule implements IInitializingModule
{
	public static boolean handlesDatabaseProtocol(String databaseProtocol)
	{
		return databaseProtocol.toLowerCase().startsWith("jdbc:sqlite");
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
		beanContextFactory.registerBean(SQLiteConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
		beanContextFactory.registerBean(SQLiteConnectionExtension.class).autowireable(IConnectionExtension.class);
		beanContextFactory.registerBean(SQLiteDialect.class).autowireable(IConnectionDialect.class);
		beanContextFactory.registerBean(SQLiteExtendedDialect.class).autowireable(IExtendedConnectionDialect.class);
		beanContextFactory.registerBean(SQLiteSequencePrimaryKeyProvider.class).autowireable(IPrimaryKeyProvider.class);

		// TODO
		// IBeanConfiguration stringToCharacterInputSourceConverter = beanContextFactory.registerBean(StringToCharacterInputSourceConverter.class);
		// beanContextFactory.link(stringToCharacterInputSourceConverter).to(IDedicatedConverterExtendable.class).with(String.class,
		// ICharacterInputSource.class);
	}
}
