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
 * Interface for linking a bean (extension/plugin) to an extendable at runtime using a fluent
 * interface.
 *
 * @see ILinkExtendable
 */
public interface ILinkRuntimeExtendable {
	/**
	 * Start linking a named bean.
	 *
	 * @param listenerBeanName Name of the bean to add to a registry.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededRuntime<?> link(String listenerBeanName);

	/**
	 * Start linking a named bean to a specific registry method.
	 *
	 * @param listenerBeanName Name of the bean to add to a registry.
	 * @param methodName Name of the registry method.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededRuntime<?> link(String listenerBeanName, String methodName);

	/**
	 * Start linking a bean identified by its {@link IBeanConfiguration}.
	 *
	 * @param listenerBean Identifier for the bean to add to a registry.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededRuntime<?> link(IBeanConfiguration listenerBean);

	/**
	 * Start linking a bean identified by its {@link IBeanConfiguration} to a specific registry
	 * method.
	 *
	 * @param listenerBean Identifier for the bean to add to a registry.
	 * @param methodName Name of the registry method.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededRuntime<?> link(IBeanConfiguration listenerBean, String methodName);

	/**
	 * Start linking a object or bean instance.
	 *
	 * @param listener Object or bean to add to a registry.
	 * @return Next step of the fluent interface.
	 */
	<D> ILinkRegistryNeededRuntime<D> link(D listener);

	/**
	 * Start linking a object or bean instance to a specific registry method.
	 *
	 * @param listener Object or bean to add to a registry.
	 * @param methodName Name of the registry method.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededRuntime<?> link(Object listener, String methodName);
}
