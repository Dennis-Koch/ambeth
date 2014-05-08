package de.osthus.ambeth.testutil;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.connection.IPreparedStatementParamLogger;

@FrameworkModule
public class PreparedStatementParamLoggerModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("preparedStatementParamLoggerFactory", PreparedStatementParamLoggerFactory.class).autowireable(
				IPreparedStatementParamLogger.class);
	}
}
