package de.osthus.ambeth.ioc;

import de.osthus.ambeth.expr.bytecode.PropertyExpressionBehavior;
import de.osthus.ambeth.expr.bytecode.PropertyExpressionMixin;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

@FrameworkModule
public class ExprModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, PropertyExpressionBehavior.class);

		beanContextFactory.registerBean(PropertyExpressionMixin.class).autowireable(PropertyExpressionMixin.class);
	}
}
