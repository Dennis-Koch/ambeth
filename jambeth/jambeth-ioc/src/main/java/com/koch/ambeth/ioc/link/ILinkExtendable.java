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

import com.koch.ambeth.ioc.config.IBeanConfiguration;

/**
 * Interface for linking a bean (extension/plugin) to an extendable during bean context startup using a fluent interface.
 * <p>
 * There are a lot of different paths through the interface. But the steps are always the same:<br>
 * - define the bean<br>
 * - define the extendable<br>
 * - optionally set key(s)<br>
 * - optionally set the link to optional
 * <p>
 * Example code registering a named bean and linking it to an extendable with two criteria:
 * <p>
 * beanContextFactory.registerBean("intConverter", IntegerConverter.class);<br>
 * beanContextFactory.link("intConverter").to(IDedicatedConverterExtendable.class).with(Integer.class, String.class);<br>
 * beanContextFactory.link("intConverter").to(IDedicatedConverterExtendable.class).with(String.class, Integer.class);
 */
public interface ILinkExtendable
{
	/**
	 * Start linking a named bean.
	 * 
	 * @param listenerBeanName
	 *            Name of the bean to add to a registry.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededConfiguration<?> link(String listenerBeanName);

	/**
	 * Start linking a named bean to a specific registry method.
	 * 
	 * @param listenerBeanName
	 *            Name of the bean to add to a registry.
	 * @param methodName
	 *            Name of the registry method.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededConfiguration<?> link(String listenerBeanName, String methodName);

	/**
	 * Start linking a bean identified by its {@link IBeanConfiguration}.
	 * 
	 * @param listenerBean
	 *            Identifier for the bean to add to a registry.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededConfiguration<?> link(IBeanConfiguration listenerBean);

	/**
	 * Start linking a bean identified by its {@link IBeanConfiguration} to a specific registry method.
	 * 
	 * @param listenerBean
	 *            Identifier for the bean to add to a registry.
	 * @param methodName
	 *            Name of the registry method.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededConfiguration<?> link(IBeanConfiguration listenerBean, String methodName);

	/**
	 * Start linking a object or bean instance.
	 * 
	 * @param listener
	 *            Object or bean to add to a registry.
	 * @return Next step of the fluent interface.
	 */
	<D> ILinkRegistryNeededConfiguration<D> link(D listener);

	/**
	 * Start linking a object or bean instance to a specific registry method.
	 * 
	 * @param listener
	 *            Object or bean to add to a registry.
	 * @param methodName
	 *            Name of the registry method.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededConfiguration<?> link(Object listener, String methodName);

	/**
	 * Links a named bean to an autowired registry.
	 * 
	 * @deprecated Replaced by fluent interface to cover more use cases with less methods.
	 * 
	 * @param listenerBeanName
	 *            Name of the bean to add to registry.
	 * @param autowiredRegistryClass
	 *            Autowire interface of the registry.
	 */
	@Deprecated
	void link(String listenerBeanName, Class<?> autowiredRegistryClass);

	/**
	 * Links a named bean to an autowired registry for given arguments.
	 * 
	 * @deprecated Replaced by fluent interface to cover more use cases with less methods.
	 * 
	 * @param listenerBeanName
	 *            Name of the bean to add to registry.
	 * @param autowiredRegistryClass
	 *            Autowire interface of the registry.
	 * @param arguments
	 *            Keys to register the bean for.
	 */
	@Deprecated
	void link(String listenerBeanName, Class<?> autowiredRegistryClass, Object... arguments);

	/**
	 * Links a bean by its {@link IBeanConfiguration} to an autowired registry for given arguments.
	 * 
	 * @deprecated Replaced by fluent interface to cover more use cases with less methods.
	 * 
	 * @param listenerBean
	 *            Identifier for the bean to add to registry.
	 * @param autowiredRegistryClass
	 *            Autowire interface of the registry.
	 */
	@Deprecated
	void link(IBeanConfiguration listenerBean, Class<?> autowiredRegistryClass);

	/**
	 * Links a bean by its {@link IBeanConfiguration} to an autowired registry for given arguments.
	 * 
	 * @deprecated Replaced by fluent interface to cover more use cases with less methods.
	 * 
	 * @param listenerBean
	 *            Identifier for the bean to add to registry.
	 * @param autowiredRegistryClass
	 *            Autowire interface of the registry.
	 * @param arguments
	 *            Keys to register the bean for.
	 */
	@Deprecated
	void link(IBeanConfiguration listenerBean, Class<?> autowiredRegistryClass, Object... arguments);

	/**
	 * Links a named bean to a named registry.
	 * 
	 * @deprecated Replaced by fluent interface to cover more use cases with less methods.
	 * 
	 * @param registryBeanName
	 *            Name of the registry.
	 * @param listenerBeanName
	 *            Name of the bean to add to registry.
	 * @param registryClass
	 *            Class/Interface containing the registry methods.
	 */
	@Deprecated
	void linkToNamed(String registryBeanName, String listenerBeanName, Class<?> registryClass);

	/**
	 * Links a name bean to an autowired registry.
	 * 
	 * @deprecated Replaced by fluent interface to cover more use cases with less methods.
	 * 
	 * @param registryBeanName
	 *            Name of the registry.
	 * @param listenerBeanName
	 *            Name of the bean to add to registry.
	 * @param registryClass
	 *            Class/Interface containing the registry methods.
	 * @param arguments
	 *            Keys to register the bean for.
	 */
	@Deprecated
	void linkToNamed(String registryBeanName, String listenerBeanName, Class<?> registryClass, Object... arguments);
}
