package de.osthus.ambeth.testutil;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.connection.IPreparedStatementParamLogger;

@FrameworkModule
public class PreparedStatementParamLoggerModule implements IInitializingModule
{
	@Property(name = TestUtilConfigurationConstants.ParamLoggerActive, defaultValue = "false")
	protected boolean paramLoggerActive;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (paramLoggerActive)
		{
			beanContextFactory.registerBean("preparedStatementParamLoggerFactory", PreparedStatementParamLoggerFactory.class).autowireable(
					IPreparedStatementParamLogger.class);
		}
	}
}
