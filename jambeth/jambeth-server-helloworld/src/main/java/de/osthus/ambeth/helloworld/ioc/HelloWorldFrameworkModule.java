package de.osthus.ambeth.helloworld.ioc;

import de.osthus.ambeth.helloworld.security.HelloWorldAuthorizationManager;
import de.osthus.ambeth.helloworld.security.HelloworldUserResolver;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.security.IAuthorizationManager;
import de.osthus.ambeth.security.IUserResolver;

@FrameworkModule
public class HelloWorldFrameworkModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("userHandleFactory", HelloWorldAuthorizationManager.class).autowireable(IAuthorizationManager.class);
		beanContextFactory.registerAnonymousBean(HelloworldUserResolver.class).autowireable(IUserResolver.class);
	}
}
