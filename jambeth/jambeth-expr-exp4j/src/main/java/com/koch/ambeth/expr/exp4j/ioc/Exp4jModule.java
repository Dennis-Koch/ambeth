package com.koch.ambeth.expr.exp4j.ioc;

import com.koch.ambeth.expr.IEntityPropertyExpressionResolver;
import com.koch.ambeth.expr.exp4j.EntityPropertyExpressionResolver;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

@FrameworkModule
public class Exp4jModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(EntityPropertyExpressionResolver.class).autowireable(IEntityPropertyExpressionResolver.class);
	}
}
