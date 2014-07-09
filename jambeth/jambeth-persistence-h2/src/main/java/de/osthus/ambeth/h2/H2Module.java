package de.osthus.ambeth.h2;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IPropertyLoadingBean;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.IConnectionExtension;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.sql.IPrimaryKeyProvider;

@FrameworkModule
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
		if (!externalTransactionManager && !databaseBehaviourStrict)
		{
			beanContextFactory.registerAnonymousBean(H2TestDialect.class).autowireable(IConnectionDialect.class);
		}
		else
		{
			beanContextFactory.registerAnonymousBean(H2TestDialect.class).autowireable(IConnectionDialect.class);
			if (externalTransactionManager && integratedConnectionPool && log.isWarnEnabled())
			{
				if (!databasePassivate)
				{
					log.warn("The Ambeth connection pool might cause problems with the external transaction manager together with deactivated passivation of connections");
				}
			}
		}
		beanContextFactory.registerBean("oracleSequencePrimaryKeyProvider", H2SequencePrimaryKeyProvider.class).autowireable(IPrimaryKeyProvider.class);

		beanContextFactory.registerAnonymousBean(H2ConnectionExtension.class).autowireable(IConnectionExtension.class);
	}
}
