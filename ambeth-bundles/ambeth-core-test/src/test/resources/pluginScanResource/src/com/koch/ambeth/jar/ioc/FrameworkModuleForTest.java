package com.koch.ambeth.jar.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

@FrameworkModule
public class FrameworkModuleForTest implements IInitializingModule
{
	static
	{
		@SuppressWarnings("unused")
		IModuleServiceForTestB b = new ModuleServiceForTestB();
		System.out.println("FrameworkModuleForTest -- static");
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("moduleServiceForTestB", ModuleServiceForTestB.class).autowireable(IModuleServiceForTestB.class);
	}
}
