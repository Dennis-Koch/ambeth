package com.koch.ambeth.persistence.sqlite;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.IExtendedConnectionDialect;
import com.koch.ambeth.persistence.api.IPrimaryKeyProvider;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.IConnectionExtension;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;

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
