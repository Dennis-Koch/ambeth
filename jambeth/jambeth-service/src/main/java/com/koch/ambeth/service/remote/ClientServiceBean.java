package com.koch.ambeth.service.remote;

/*-
 * #%L
 * jambeth-service
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

import com.koch.ambeth.ioc.IFactoryBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.util.proxy.IProxyFactory;

import net.sf.cglib.proxy.MethodInterceptor;

public class ClientServiceBean implements IFactoryBean, IInitializingBean {
	public static final String INTERFACE_PROP_NAME = "InterfaceType";
	public static final String SYNC_REMOTE_INTERFACE_PROP_NAME = "SyncRemoteInterfaceType";
	public static final String ASYNC_REMOTE_INTERFACE_PROP_NAME = "AsyncRemoteInterfaceType";

	@Autowired
	protected IClientServiceFactory clientServiceFactory;

	@Autowired
	protected IClientServiceInterceptorBuilder clientServiceInterceptorBuilder;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Autowired
	protected IServiceContext beanContext;

	@Property
	protected Class<?> interfaceType;

	@Property(mandatory = false)
	protected Class<?> syncRemoteInterfaceType;

	@Property(mandatory = false)
	protected Class<?> asyncRemoteInterfaceType;

	public Object proxy;

	@Override
	public void afterPropertiesSet() throws Throwable {
		getObject();
	}

	private void init() {
		MethodInterceptor interceptor = clientServiceInterceptorBuilder.createInterceptor(beanContext,
				interfaceType, syncRemoteInterfaceType, asyncRemoteInterfaceType);
		proxy = proxyFactory.createProxy(interfaceType, interceptor);
	}

	@Override
	public Object getObject() {
		if (proxy == null) {
			init();
		}
		return proxy;
	}
}
