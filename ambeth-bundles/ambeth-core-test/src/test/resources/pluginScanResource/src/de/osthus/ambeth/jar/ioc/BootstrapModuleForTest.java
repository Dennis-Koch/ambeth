package de.osthus.ambeth.jar.ioc;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.BootstrapModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

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
