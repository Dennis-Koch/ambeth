package com.koch.ambeth.ioc;

/*-
 * #%L
 * jambeth-mina
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import org.apache.mina.core.session.IdleStatusChecker;

import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.mina.client.IMinaClient;
import com.koch.ambeth.mina.client.IdleStatusCheckerShutdownHook;
import com.koch.ambeth.mina.client.MinaClient;
import com.koch.ambeth.mina.server.IMinaServerNio;
import com.koch.ambeth.mina.server.MinaServerNio;

@FrameworkModule
public class MinaModule implements IInitializingModule {

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerAutowireableBean(IMinaClient.class, MinaClient.class);
		beanContextFactory.registerAutowireableBean(IMinaServerNio.class, MinaServerNio.class);
		beanContextFactory.registerAutowireableBean(IdleStatusChecker.class, IdleStatusChecker.class);
		beanContextFactory.registerBean(IdleStatusCheckerShutdownHook.class);
	}

}
