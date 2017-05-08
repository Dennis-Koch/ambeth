package com.koch.ambeth.ioc.config;

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

import java.lang.reflect.Modifier;

import com.koch.ambeth.ioc.exception.BeanContextDeclarationException;
import com.koch.ambeth.ioc.proxy.EmptyInterceptor;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.IProxyFactory;

public class BeanConfiguration extends AbstractBeanConfiguration {
	protected final Class<?> beanType;

	protected Object createdInstance;

	protected boolean isAbstract;

	protected final IProxyFactory proxyFactory;

	public BeanConfiguration(Class<?> beanType, String beanName, IProxyFactory proxyFactory,
			IProperties props) {
		super(beanName, props);
		this.beanType = beanType;
		this.proxyFactory = proxyFactory;
	}

	@Override
	public IBeanConfiguration template() {
		isAbstract = true;
		return this;
	}

	@Override
	public Class<?> getBeanType() {
		return beanType;
	}

	@Override
	public boolean isAbstract() {
		return isAbstract;
	}

	@Override
	public Object getInstance(Class<?> instanceType) {
		if (createdInstance == null) {
			try {
				if (instanceType.isInterface() || Modifier.isAbstract(instanceType.getModifiers())) {
					createdInstance = proxyFactory.createProxy(instanceType, EmptyInterceptor.INSTANCE);
				}
				else {
					createdInstance = instanceType.newInstance();
					if (declarationStackTrace != null
							&& createdInstance instanceof IDeclarationStackTraceAware) {
						((IDeclarationStackTraceAware) createdInstance)
								.setDeclarationStackTrace(declarationStackTrace);
					}
				}
			}
			catch (Throwable e) {
				if (declarationStackTrace != null) {
					throw new BeanContextDeclarationException(declarationStackTrace, e);
				}
				else {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
		return createdInstance;
	}
}
