package de.osthus.ambeth.oracle;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IPropertyLoadingBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.sql.IPrimaryKeyProvider;
import de.osthus.ambeth.util.IDedicatedConverterExtendable;

public class Oracle10gSimpleModule implements IInitializingModule, IPropertyLoadingBean
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
	public void applyProperties(Properties contextProperties)
	{
		contextProperties.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionInterfaces, "oracle.jdbc.OracleConnection");
		contextProperties.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, Oracle10gConnectionModule.class.getName());
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (!externalTransactionManager && !databaseBehaviourStrict)
		{
			beanContextFactory.registerBean("connectionDialect", Oracle10gThinDialect.class).autowireable(IConnectionDialect.class);
		}
		else
		{
			beanContextFactory.registerBean("connectionDialect", Oracle10gTestDialect.class).autowireable(IConnectionDialect.class);
			if (externalTransactionManager && integratedConnectionPool && log.isWarnEnabled())
			{
				if (!databasePassivate)
				{
					log.warn("The Ambeth connection pool might cause problems with the external transaction manager together with deactivated passivation of connections");
				}
			}
		}
		beanContextFactory.registerBean("oracleSequencePrimaryKeyProvider", Oracle10gSequencePrimaryKeyProvider.class).autowireable(IPrimaryKeyProvider.class);

		beanContextFactory.registerBean("oracleTimestampConverter", OracleTimestampConverter.class);
		beanContextFactory.link("oracleTimestampConverter").to(IDedicatedConverterExtendable.class).with(oracle.sql.TIMESTAMP.class, Long.class);
		beanContextFactory.link("oracleTimestampConverter").to(IDedicatedConverterExtendable.class).with(oracle.sql.TIMESTAMP.class, Long.TYPE);
		beanContextFactory.link("oracleTimestampConverter").to(IDedicatedConverterExtendable.class).with(oracle.sql.TIMESTAMP.class, Date.class);
		beanContextFactory.link("oracleTimestampConverter").to(IDedicatedConverterExtendable.class).with(oracle.sql.TIMESTAMP.class, Calendar.class);

		beanContextFactory.registerBean("oracleArrayConverter", OracleArrayConverter.class);
		beanContextFactory.link("oracleArrayConverter").to(IDedicatedConverterExtendable.class).with(oracle.sql.ARRAY.class, Collection.class);
		beanContextFactory.link("oracleArrayConverter").to(IDedicatedConverterExtendable.class).with(oracle.sql.ARRAY.class, String.class);
	}
}
