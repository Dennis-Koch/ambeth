package com.koch.ambeth.ioc;

import org.apache.mina.core.session.IdleStatusChecker;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.mina.client.IMinaClient;
import com.koch.ambeth.mina.client.IdleStatusCheckerShutdownHook;
import com.koch.ambeth.mina.client.MinaClient;
import com.koch.ambeth.mina.server.IMinaServerNio;
import com.koch.ambeth.mina.server.MinaServerNio;

@FrameworkModule
public class MinaModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAutowireableBean(IMinaClient.class, MinaClient.class);
		beanContextFactory.registerAutowireableBean(IMinaServerNio.class, MinaServerNio.class);
		beanContextFactory.registerAutowireableBean(IdleStatusChecker.class, IdleStatusChecker.class);
		beanContextFactory.registerBean(IdleStatusCheckerShutdownHook.class);
	}

}
