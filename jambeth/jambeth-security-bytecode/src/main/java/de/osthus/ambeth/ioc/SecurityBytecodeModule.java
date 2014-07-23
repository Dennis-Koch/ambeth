package de.osthus.ambeth.ioc;

import de.osthus.ambeth.bytecode.behavior.EntityPrivilegeBehavior;
import de.osthus.ambeth.bytecode.behavior.EntityTypePrivilegeBehavior;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

@FrameworkModule
public class SecurityBytecodeModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, EntityPrivilegeBehavior.class);
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, EntityTypePrivilegeBehavior.class);

		// beanContextFactory.registerAnonymousBean(ValueHolderContainerTemplate.class).autowireable(ValueHolderContainerTemplate.class);
	}
}
