package com.koch.ambeth.server.helloworld.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.security.IAuthorizationManager;
import com.koch.ambeth.security.server.IUserResolver;
import com.koch.ambeth.server.helloworld.security.HelloWorldAuthorizationManager;
import com.koch.ambeth.server.helloworld.security.HelloworldUserResolver;

@FrameworkModule
public class HelloWorldFrameworkModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("userHandleFactory", HelloWorldAuthorizationManager.class).autowireable(IAuthorizationManager.class);
		beanContextFactory.registerBean(HelloworldUserResolver.class).autowireable(IUserResolver.class);
	}
}
