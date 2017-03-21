package com.koch.ambeth.ioc.link;

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

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;

/**
 * Service bean for registering a bean (extension/plugin) to extendables at runtime using a fluent interface.
 * 
 * @see ILinkExtendable
 */
public interface ILinkController
{
	/**
	 * Start linking a named bean.
	 * 
	 * @param serviceContext
	 *            IoC context containing the bean and the registry.
	 * @param listenerBeanName
	 *            Name of the bean to add to a registry.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, String listenerBeanName);

	/**
	 * Start linking a named bean to a specific registry method.
	 * 
	 * @param serviceContext
	 *            IoC context containing the bean and the registry.
	 * @param listenerBeanName
	 *            Name of the bean to add to a registry.
	 * @param methodName
	 *            Name of the registry method.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, String listenerBeanName, String methodName);

	/**
	 * Start linking a bean identified by its {@link IBeanConfiguration}.
	 * 
	 * @param serviceContext
	 *            IoC context containing the bean and the registry.
	 * @param listenerBean
	 *            Identifier for the bean to add to a registry.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, IBeanConfiguration listenerBean);

	/**
	 * Start linking a bean identified by its {@link IBeanConfiguration} to a specific registry method.
	 * 
	 * @param serviceContext
	 *            IoC context containing the bean and the registry.
	 * @param listenerBean
	 *            Identifier for the bean to add to a registry.
	 * @param methodName
	 *            Name of the registry method.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, IBeanConfiguration listenerBean, String methodName);

	/**
	 * Start linking a object or bean instance.
	 * 
	 * @param serviceContext
	 *            IoC context containing the bean and the registry.
	 * @param listener
	 *            Object or bean to add to a registry.
	 * @return Next step of the fluent interface.
	 */
	<D> ILinkRegistryNeededRuntime<D> link(IServiceContext serviceContext, D listener);

	/**
	 * Start linking a object or bean instance to a specific registry method.
	 * 
	 * @param serviceContext
	 *            IoC context containing the bean and the registry.
	 * @param listener
	 *            Object or bean to add to a registry.
	 * @param methodName
	 *            Name of the registry method.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, Object listener, String methodName);

	/**
	 * Start linking a named bean to a specific registry method without using the fluent interface.
	 * 
	 * @param listenerBeanName
	 *            Name of the bean to add to a registry.
	 * @param methodName
	 *            Name of the registry method.
	 * @return The LinkConfiguration object.
	 */
	LinkConfiguration<Object> createLinkConfiguration(String listenerBeanName, String methodName);

	/**
	 * Start linking a bean identified by its {@link IBeanConfiguration} to a specific registry method without using the fluent interface.
	 * 
	 * @param listenerBean
	 *            Identifier for the bean to add to a registry.
	 * @param methodName
	 *            Name of the registry method.
	 * @return The LinkConfiguration object.
	 */
	LinkConfiguration<Object> createLinkConfiguration(IBeanConfiguration listenerBean, String methodName);

	/**
	 * Start linking a object or bean instance to a specific registry method without using the fluent interface.
	 * 
	 * @param listener
	 *            Object or bean to add to a registry.
	 * @param methodName
	 *            Name of the registry method.
	 * @return The LinkConfiguration object.
	 */
	LinkConfiguration<Object> createLinkConfiguration(Object listener, String methodName);

	/**
	 * Start linking a object or bean instance without using the fluent interface.
	 * 
	 * @param listener
	 *            Object or bean to add to a registry.
	 * @return The LinkConfiguration object.
	 */
	<D> LinkConfiguration<D> createLinkConfiguration(D listener);

	@Deprecated
	IBeanConfiguration createLinkConfiguration(String registryBeanName, String listenerBeanName, Class<?> registryClass);

	@Deprecated
	IBeanConfiguration createLinkConfiguration(String registryBeanName, String listenerBeanName, Class<?> registryClass, Object... arguments);

	@Deprecated
	IBeanConfiguration createLinkConfiguration(String listenerBeanName, Class<?> autowiredRegistryClass);

	@Deprecated
	IBeanConfiguration createLinkConfiguration(String listenerBeanName, Class<?> autowiredRegistryClass, Object... arguments);

	@Deprecated
	void link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, Class<?> registryClass, Object... arguments);

	@Deprecated
	void link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, Class<?> registryClass);

	@Deprecated
	void link(IServiceContext serviceContext, IBeanConfiguration listenerBean, Class<?> autowiredRegistryClass);

	@Deprecated
	void link(IServiceContext serviceContext, IBeanConfiguration listenerBean, Class<?> autowiredRegistryClass, Object... arguments);

	@Deprecated
	void link(IServiceContext serviceContext, String listenerBeanName, Class<?> autowiredRegistryClass);

	@Deprecated
	void link(IServiceContext serviceContext, String listenerBeanName, Class<?> autowiredRegistryClass, Object... arguments);
}
