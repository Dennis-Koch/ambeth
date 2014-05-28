package de.osthus.ambeth.ioc;

import de.osthus.ambeth.compositeid.CompositeIdBehavior;
import de.osthus.ambeth.compositeid.CompositeIdFactory;
import de.osthus.ambeth.compositeid.ICompositeIdFactory;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

@FrameworkModule
public class CompositeIdModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("compositeIdFactory", CompositeIdFactory.class).autowireable(ICompositeIdFactory.class);

		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, CompositeIdBehavior.class);
	}
}
