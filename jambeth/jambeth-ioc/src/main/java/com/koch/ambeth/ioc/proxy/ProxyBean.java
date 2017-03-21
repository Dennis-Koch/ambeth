package com.koch.ambeth.ioc.proxy;

/*-
 * #%L
 * jambeth-ioc
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
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.proxy.IProxyFactory;

import net.sf.cglib.proxy.MethodInterceptor;

public class ProxyBean implements IInitializingBean, IFactoryBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IProxyFactory proxyFactory;

	protected Class<?> type;

	protected Class<?>[] additionalTypes;

	protected MethodInterceptor interceptor;

	protected Object proxy;

	@Override
	public void afterPropertiesSet() {
		ParamChecker.assertNotNull(proxyFactory, "ProxyFactory");
		ParamChecker.assertNotNull(type, "Type");

		getProxy();
	}

	public void setAdditionalTypes(Class<?>[] additionalTypes) {
		this.additionalTypes = additionalTypes;
	}

	public void setInterceptor(MethodInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	public void setProxyFactory(IProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	protected Object getProxy() {
		if (proxy == null) {
			if (interceptor != null) {
				if (additionalTypes != null) {
					proxy = proxyFactory.createProxy(getClass().getClassLoader(), type, additionalTypes,
							interceptor);
				}
				else {
					proxy = proxyFactory.createProxy(getClass().getClassLoader(), type, interceptor);
				}
			}
			else if (additionalTypes != null) {
				proxy = proxyFactory.createProxy(getClass().getClassLoader(), type, additionalTypes);
			}
			else {
				proxy = proxyFactory.createProxy(getClass().getClassLoader(), type);
			}
		}
		return proxy;
	}

	@Override
	public Object getObject() {
		return getProxy();
	}
}
