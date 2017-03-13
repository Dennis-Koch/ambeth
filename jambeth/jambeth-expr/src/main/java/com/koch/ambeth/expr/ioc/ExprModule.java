package com.koch.ambeth.expr.ioc;

import com.koch.ambeth.bytecode.ioc.BytecodeModule;
import com.koch.ambeth.expr.bytecode.PropertyExpressionBehavior;
import com.koch.ambeth.expr.bytecode.PropertyExpressionMixin;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

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
