package com.koch.ambeth.testutil;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.jdbc.connection.IPreparedStatementParamLogger;

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
