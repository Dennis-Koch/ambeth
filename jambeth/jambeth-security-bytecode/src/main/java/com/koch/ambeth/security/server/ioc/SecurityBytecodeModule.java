package com.koch.ambeth.security.server.ioc;

import com.koch.ambeth.bytecode.ioc.BytecodeModule;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.security.server.bytecode.behavior.EntityPrivilegeBehavior;
import com.koch.ambeth.security.server.bytecode.behavior.EntityTypePrivilegeBehavior;

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
