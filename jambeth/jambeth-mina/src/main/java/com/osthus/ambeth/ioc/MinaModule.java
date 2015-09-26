package com.osthus.ambeth.ioc;

import org.apache.mina.core.session.IdleStatusChecker;

import com.osthus.ambeth.mina.client.IMinaClient;
import com.osthus.ambeth.mina.client.IdleStatusCheckerShutdownHook;
import com.osthus.ambeth.mina.client.MinaClient;
import com.osthus.ambeth.mina.server.IMinaServerNio;
import com.osthus.ambeth.mina.server.MinaServerNio;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

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
