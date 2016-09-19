package de.osthus.ambeth.ioc;

import de.osthus.ambeth.bytecode.behavior.DelegateBehavior;
import de.osthus.ambeth.bytecode.behavior.EntityMetaDataMemberBehavior;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehavior;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehaviorExtendable;
import de.osthus.ambeth.bytecode.behavior.ObjRefBehavior;
import de.osthus.ambeth.bytecode.behavior.ObjRefStoreBehavior;
import de.osthus.ambeth.compositeid.CompositeIdBehavior;
import de.osthus.ambeth.compositeid.CompositeIdFactory;
import de.osthus.ambeth.compositeid.ICompositeIdFactory;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

@FrameworkModule
public class MergeBytecodeModule implements IInitializingModule
{
	private abstract class DisposeModule implements IInitializingBean, IDisposableBean
	{

	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("compositeIdFactory", CompositeIdFactory.class).autowireable(ICompositeIdFactory.class);

		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, CompositeIdBehavior.class);
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, EntityMetaDataMemberBehavior.class);

		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, ObjRefBehavior.class);
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, ObjRefStoreBehavior.class);

		// small trick: we need the DelegateBehavior as a very-early-registered extension to the BytecodeEnhancer
		// in the ordinary "link" phase it is already too late
		final IBeanConfiguration delegateBehavior = beanContextFactory.registerBean(DelegateBehavior.class);
		beanContextFactory.registerWithLifecycle(new DisposeModule()
		{
			@Autowired
			protected IBytecodeBehaviorExtendable bytecodeBehaviorExtendable;
			private IBytecodeBehavior instance;

			@Override
			public void afterPropertiesSet() throws Throwable
			{
				instance = (IBytecodeBehavior) delegateBehavior.getInstance();
				bytecodeBehaviorExtendable.registerBytecodeBehavior(instance);
			}

			@Override
			public void destroy() throws Throwable
			{
				bytecodeBehaviorExtendable.unregisterBytecodeBehavior(instance);
			}
		});
	}
}
