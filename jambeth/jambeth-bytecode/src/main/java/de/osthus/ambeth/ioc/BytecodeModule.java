package de.osthus.ambeth.ioc;

import de.osthus.ambeth.bytecode.IBytecodeClassLoader;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.bytecode.IBytecodePrinter;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehavior;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehaviorExtendable;
import de.osthus.ambeth.bytecode.core.BytecodeClassLoader;
import de.osthus.ambeth.bytecode.core.BytecodeEnhancer;
import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

@FrameworkModule
public class BytecodeModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("bytecodeEnhancer", BytecodeEnhancer.class).autowireable(IBytecodeEnhancer.class, IBytecodeBehaviorExtendable.class);

		IBeanConfiguration bytecodeClassLoaderBC = beanContextFactory.registerBean(BytecodeClassLoader.class).autowireable(IBytecodeClassLoader.class,
				IBytecodePrinter.class);
		beanContextFactory.link(bytecodeClassLoaderBC).to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class).optional();
	}

	public static IBeanConfiguration addDefaultBytecodeBehavior(IBeanContextFactory beanContextFactory, Class<? extends IBytecodeBehavior> behaviorType)
	{
		IBeanConfiguration behaviorBC = beanContextFactory.registerBean(behaviorType);
		beanContextFactory.link(behaviorBC).to(IBytecodeBehaviorExtendable.class);
		return behaviorBC;
	}
}
