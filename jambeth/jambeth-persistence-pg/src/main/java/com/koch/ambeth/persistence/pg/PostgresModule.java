package com.koch.ambeth.persistence.pg;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
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
import com.koch.ambeth.stream.chars.ICharacterInputSource;
import com.koch.ambeth.util.IDedicatedConverterExtendable;

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
		beanContextFactory.registerBean(PostgresConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
		beanContextFactory.registerBean(PostgresConnectionExtension.class).autowireable(IConnectionExtension.class);
		beanContextFactory.registerBean(PostgresDialect.class).autowireable(IConnectionDialect.class);
		beanContextFactory.registerBean(PostgresExtendedDialect.class).autowireable(IExtendedConnectionDialect.class);
		beanContextFactory.registerBean(PostgresSequencePrimaryKeyProvider.class).autowireable(IPrimaryKeyProvider.class);

		IBeanConfiguration stringToCharacterInputSourceConverter = beanContextFactory.registerBean(StringToCharacterInputSourceConverter.class);
		beanContextFactory.link(stringToCharacterInputSourceConverter).to(IDedicatedConverterExtendable.class).with(String.class, ICharacterInputSource.class);
	}
}
