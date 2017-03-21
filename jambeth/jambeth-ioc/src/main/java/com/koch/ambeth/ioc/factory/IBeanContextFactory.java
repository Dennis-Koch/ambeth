package com.koch.ambeth.ioc.factory;

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

import com.koch.ambeth.ioc.IAnonymousBeanRegistry;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.link.ILinkExtendable;

/**
 * This interface is used in initializing modules to add beans and configurations to the starting IoC context.
 */
public interface IBeanContextFactory extends ILinkExtendable, IAnonymousBeanRegistry
{
	/**
	 * Used to give a bean multiple names.
	 * 
	 * @param aliasBeanName
	 *            Additional name.
	 * @param beanNameToCreateAliasFor
	 *            Currently used name.
	 */
	void registerAlias(String aliasBeanName, String beanNameToCreateAliasFor);

	/**
	 * Registers a named bean that inherits the configuration from an abstract parent bean configuration.
	 * 
	 * @param beanName
	 *            Given name of the bean instance. Used for referencing the bean and lookups.
	 * @param parentBeanName
	 *            Given name of the parent bean configuration.
	 * @return
	 */
	IBeanConfiguration registerBean(String beanName, String parentBeanName);

	/**
	 * Primary method to add a bean to the context.
	 * 
	 * @param beanName
	 *            Given name of the bean instance. Used for referencing the bean and lookups.
	 * @param beanType
	 *            Class of the bean to be instantiated.
	 * @return The bean configuration instance to add properties and configurations.
	 */
	IBeanConfiguration registerBean(String beanName, Class<?> beanType);

	/**
	 * Registers an anonymous bean and auto-wires it. Usage is discouraged.
	 * 
	 * @param interfaceType
	 *            Interface to autowire the bean to.
	 * @param beanType
	 *            Class of the bean to be instantiated.
	 * @return The bean configuration instance to add properties and configurations.
	 */
	<I, T extends I> IBeanConfiguration registerAutowireableBean(Class<I> interfaceType, Class<T> beanType);

	/**
	 * To register an already instantiated bean in the context. Injections will be made and life cycle methods will be called.
	 * 
	 * @param beanName
	 *            Given name of the bean instance. Used for referencing the bean and lookups.
	 * @param object
	 *            Bean instance to register.
	 * @return The bean configuration instance to add properties and configurations.
	 */
	IBeanConfiguration registerWithLifecycle(String beanName, Object object);

	/**
	 * To register an already instantiated bean in the context. Injections will be made, life cycle methods will not be called.
	 * 
	 * @param beanName
	 *            Given name of the bean instance. Used for referencing the bean and lookups.
	 * @param externalBean
	 *            Bean instance to register.
	 * @return The bean configuration instance to add properties and configurations.
	 */
	IBeanConfiguration registerExternalBean(String beanName, Object externalBean);
}
