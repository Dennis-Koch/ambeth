package com.koch.ambeth.jar.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.BootstrapModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

@BootstrapModule
public class BootstrapModuleForTest implements IInitializingModule
{
	static
	{
		@SuppressWarnings("unused")
		IModuleServiceForTestA a = new ModuleServiceForTestA();
		System.out.println("BootstrapModuleForTest -- static");
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("moduleServiceForTestA", ModuleServiceForTestA.class).autowireable(IModuleServiceForTestA.class);
	}
}
