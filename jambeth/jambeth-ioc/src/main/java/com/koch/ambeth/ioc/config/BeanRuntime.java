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

import com.koch.ambeth.ioc.IBeanRuntime;
import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.factory.IBeanContextInitializer;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.proxy.IProxyFactory;

public class BeanRuntime<V> implements IBeanRuntime<V>
{
	protected ServiceContext serviceContext;

	protected BeanConfiguration beanConfiguration;

	protected boolean joinLifecycle;

	protected V beanInstance;

	protected Class<? extends V> beanType;

	public BeanRuntime(ServiceContext serviceContext, Class<? extends V> beanType, boolean joinLifecycle)
	{
		this.serviceContext = serviceContext;
		this.beanType = beanType;
		this.joinLifecycle = joinLifecycle;
		beanConfiguration = createBeanConfiguration(beanType);
	}

	public BeanRuntime(ServiceContext serviceContext, V beanInstance, boolean joinLifecycle)
	{
		this.serviceContext = serviceContext;
		this.beanInstance = beanInstance;
		this.joinLifecycle = joinLifecycle;
		beanConfiguration = createBeanConfiguration(beanInstance.getClass());
	}

	protected BeanConfiguration createBeanConfiguration(Class<?> beanType)
	{
		IProxyFactory proxyFactory = serviceContext.getService(IProxyFactory.class, false);
		IProperties props = serviceContext.getService(IProperties.class, true);
		return new BeanConfiguration(beanType, null, proxyFactory, props);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V finish()
	{
		BeanContextFactory beanContextFactory = serviceContext.getBeanContextFactory();
		IBeanContextInitializer beanContextInitializer = beanContextFactory.getBeanContextInitializer();
		IList<IBeanConfiguration> beanConfHierarchy = beanContextInitializer.fillParentHierarchyIfValid(serviceContext, beanContextFactory, beanConfiguration);

		V bean = beanInstance;
		if (bean == null)
		{
			Class<?> beanType = this.beanType;
			if (beanType == null)
			{
				beanType = beanContextInitializer.resolveTypeInHierarchy(beanConfHierarchy);
			}
			bean = (V) beanContextInitializer.instantiateBean(serviceContext, beanContextFactory, beanConfiguration, beanType, beanConfHierarchy);
		}
		bean = (V) beanContextInitializer.initializeBean(serviceContext, beanContextFactory, beanConfiguration, bean, beanConfHierarchy, joinLifecycle);
		return bean;
	}

	@Override
	public IBeanRuntime<V> parent(String parentBeanTemplateName)
	{
		beanConfiguration.parent(parentBeanTemplateName);
		return this;
	}

	@Override
	public IBeanRuntime<V> propertyRef(String propertyName, String beanName)
	{
		beanConfiguration.propertyRef(propertyName, beanName);
		return this;
	}

	@Override
	public IBeanRuntime<V> propertyRefFromContext(String propertyName, String fromContext, String beanName)
	{
		beanConfiguration.propertyRefFromContext(propertyName, fromContext, beanName);
		return this;
	}

	@Override
	public IBeanRuntime<V> propertyRef(String propertyName, IBeanConfiguration bean)
	{
		beanConfiguration.propertyRef(propertyName, bean);
		return this;
	}

	@Override
	public IBeanRuntime<V> propertyRefs(String beanName)
	{
		beanConfiguration.propertyRefs(beanName);
		return this;
	}

	@Override
	public IBeanRuntime<V> propertyRefs(String... beanNames)
	{
		beanConfiguration.propertyRefs(beanNames);
		return this;
	}

	@Override
	public IBeanRuntime<V> propertyRef(IBeanConfiguration bean)
	{
		beanConfiguration.propertyRef(bean);
		return this;
	}

	@Override
	public IBeanRuntime<V> propertyValue(String propertyName, Object value)
	{
		beanConfiguration.propertyValue(propertyName, value);
		return this;
	}

	@Override
	public IBeanRuntime<V> ignoreProperties(String propertyName)
	{
		beanConfiguration.ignoreProperties(propertyName);
		return this;
	}

	@Override
	public IBeanRuntime<V> ignoreProperties(String... propertyNames)
	{
		beanConfiguration.ignoreProperties(propertyNames);
		return this;
	}
}
