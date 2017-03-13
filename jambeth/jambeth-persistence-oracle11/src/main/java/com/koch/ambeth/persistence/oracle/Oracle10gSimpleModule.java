package com.koch.ambeth.persistence.oracle;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

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
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;
import com.koch.ambeth.util.IDedicatedConverterExtendable;

public class Oracle10gSimpleModule implements IInitializingModule
{
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
		beanContextFactory.registerBean(OracleConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
		beanContextFactory.registerBean(Oracle10gExtendedDialect.class).autowireable(IExtendedConnectionDialect.class);
		if (!externalTransactionManager && !databaseBehaviourStrict)
		{
			beanContextFactory.registerBean(Oracle10gThinDialect.class).autowireable(IConnectionDialect.class);
		}
		else
		{
			beanContextFactory.registerBean(Oracle10gDialect.class).autowireable(IConnectionDialect.class);
			if (externalTransactionManager && integratedConnectionPool && log.isWarnEnabled())
			{
				if (!databasePassivate)
				{
					log.warn("The Ambeth connection pool might cause problems with the external transaction manager together with deactivated passivation of connections");
				}
			}
		}
		beanContextFactory.registerBean(Oracle10gSequencePrimaryKeyProvider.class).autowireable(IPrimaryKeyProvider.class);

		IBeanConfiguration timestampConverter = beanContextFactory.registerBean(OracleTimestampConverter.class);
		beanContextFactory.link(timestampConverter).to(IDedicatedConverterExtendable.class).with(oracle.sql.TIMESTAMP.class, Long.class);
		beanContextFactory.link(timestampConverter).to(IDedicatedConverterExtendable.class).with(oracle.sql.TIMESTAMP.class, Long.TYPE);
		beanContextFactory.link(timestampConverter).to(IDedicatedConverterExtendable.class).with(oracle.sql.TIMESTAMP.class, Date.class);
		beanContextFactory.link(timestampConverter).to(IDedicatedConverterExtendable.class).with(oracle.sql.TIMESTAMP.class, Calendar.class);

		IBeanConfiguration arrayConverter = beanContextFactory.registerBean(OracleArrayConverter.class);
		beanContextFactory.link(arrayConverter).to(IDedicatedConverterExtendable.class).with(oracle.sql.ARRAY.class, Collection.class);
		beanContextFactory.link(arrayConverter).to(IDedicatedConverterExtendable.class).with(oracle.sql.ARRAY.class, String.class);
	}
}
