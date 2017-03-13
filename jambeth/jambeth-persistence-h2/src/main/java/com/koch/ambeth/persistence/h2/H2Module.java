package com.koch.ambeth.persistence.h2;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IPropertyLoadingBean;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.IExtendedConnectionDialect;
import com.koch.ambeth.persistence.api.IPrimaryKeyProvider;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.IConnectionExtension;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;

public class H2Module implements IInitializingModule, IPropertyLoadingBean
{
	@SuppressWarnings("unused")
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
		// contextProperties.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionInterfaces, JdbcConnection.class.getName());
		contextProperties.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, H2ConnectionModule.class.getName());
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(H2ConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
		beanContextFactory.registerBean(H2ExtendedDialect.class).autowireable(IExtendedConnectionDialect.class);
		if (!externalTransactionManager && !databaseBehaviourStrict)
		{
			beanContextFactory.registerBean(H2Dialect.class).autowireable(IConnectionDialect.class);
		}
		else
		{
			beanContextFactory.registerBean(H2Dialect.class).autowireable(IConnectionDialect.class);
			if (externalTransactionManager && integratedConnectionPool && log.isWarnEnabled())
			{
				if (!databasePassivate)
				{
					log.warn("The Ambeth connection pool might cause problems with the external transaction manager together with deactivated passivation of connections");
				}
			}
		}
		beanContextFactory.registerBean(H2SequencePrimaryKeyProvider.class).autowireable(IPrimaryKeyProvider.class);

		beanContextFactory.registerBean(H2ConnectionExtension.class).autowireable(IConnectionExtension.class);
	}
}
