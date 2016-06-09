package de.osthus.ambeth.jar.ioc;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

@FrameworkModule
public class FrameworkModuleForTest implements IInitializingModule
{
	static
	{
		@SuppressWarnings("unused")
		IModuleServiceForTestB b = new ModuleServiceForTestB();
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("moduleServiceForTestB", ModuleServiceForTestB.class).autowireable(IModuleServiceForTestB.class);
	}
}
