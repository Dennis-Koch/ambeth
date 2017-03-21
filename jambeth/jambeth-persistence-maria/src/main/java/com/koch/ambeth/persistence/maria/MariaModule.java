package com.koch.ambeth.persistence.maria;

/*-
 * #%L
 * jambeth-persistence-maria
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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

public class MariaModule implements IInitializingModule, IPropertyLoadingBean
{
	public static boolean handlesDatabaseProtocol(String databaseProtocol)
	{
		return databaseProtocol.toLowerCase().startsWith("jdbc:mariadb");
	}

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
		contextProperties.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, MariaConnectionModule.class.getName());
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(MariaConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
		beanContextFactory.registerBean(MariaExtendedDialect.class).autowireable(IExtendedConnectionDialect.class);
		if (!externalTransactionManager && !databaseBehaviourStrict)
		{
			beanContextFactory.registerBean(MariaDialect.class).autowireable(IConnectionDialect.class);
		}
		else
		{
			beanContextFactory.registerBean(MariaDialect.class).autowireable(IConnectionDialect.class);
			if (externalTransactionManager && integratedConnectionPool && log.isWarnEnabled())
			{
				if (!databasePassivate)
				{
					log.warn("The Ambeth connection pool might cause problems with the external transaction manager together with deactivated passivation of connections");
				}
			}
		}
		beanContextFactory.registerBean(MariaSequencePrimaryKeyProvider.class).autowireable(IPrimaryKeyProvider.class);

		beanContextFactory.registerBean(MariaConnectionExtension.class).autowireable(IConnectionExtension.class);
	}
}
