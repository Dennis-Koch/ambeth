package de.osthus.ambeth.ioc;

import de.osthus.ambeth.bytecode.behavior.EntityMetaDataMemberBehavior;
import de.osthus.ambeth.bytecode.behavior.ObjRefBehavior;
import de.osthus.ambeth.bytecode.behavior.ObjRefStoreBehavior;
import de.osthus.ambeth.compositeid.CompositeIdBehavior;
import de.osthus.ambeth.compositeid.CompositeIdFactory;
import de.osthus.ambeth.compositeid.ICompositeIdFactory;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

@FrameworkModule
public class MergeBytecodeModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("compositeIdFactory", CompositeIdFactory.class).autowireable(ICompositeIdFactory.class);

		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, CompositeIdBehavior.class);
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, EntityMetaDataMemberBehavior.class);

		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, ObjRefBehavior.class);
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, ObjRefStoreBehavior.class);
	}
}
