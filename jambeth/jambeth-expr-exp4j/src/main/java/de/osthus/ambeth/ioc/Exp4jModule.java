package de.osthus.ambeth.ioc;

import de.osthus.ambeth.expr.IEntityPropertyExpressionResolver;
import de.osthus.ambeth.expr.exp4j.EntityPropertyExpressionResolver;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

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
