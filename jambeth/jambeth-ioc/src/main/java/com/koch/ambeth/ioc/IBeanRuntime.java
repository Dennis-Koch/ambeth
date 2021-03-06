package com.koch.ambeth.ioc;

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
 * Fluent interface to configure a new bean instance in a running jAmbeth IoC context.
 *
 * @see IServiceContext#registerBean(Class)
 *
 * @param <V> Type of the new bean instance.
 */
public interface IBeanRuntime<V> {
	/**
	 * Finish the bean configuration and add the new bean instance to the context.
	 *
	 * @return New bean instance.
	 */
	V finish();

	/**
	 * Set the new bean to inherit the configuration from an abstract parent bean configuration.
	 *
	 * @param parentBeanTemplateName Given name of the parent bean configuration.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> parent(String parentBeanTemplateName);

	/**
	 * Defines a named bean to be injected into a specific property of the new bean.
	 *
	 * @see IBeanConfiguration#propertyRef(String, String)
	 *
	 * @param propertyName Name of the property.
	 * @param beanName Name of the bean to be injected.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> propertyRef(String propertyName, String beanName);

	/**
	 * Specifies a property injection and references the bean to inject by its bean context name and
	 * bean name (in the given context).
	 * <p>
	 * The <tt>propertyRef()</tt> methods also allow <b>special references</b>.<br>
	 * - An asterisk/star (*) in front of the bean name ignores the directly resolved IoC context and
	 * directs the search to its parent context, even if a bean with the name exists in the directly
	 * resolved IoC context.<br>
	 * - An ampersand (&) in front of the bean name requests the FactoryBean with this name. This is
	 * only useful if the bean in fact is an FactoryBean.
	 *
	 * @param propertyName Name of the target property.
	 * @param fromContext Name of the bean context where the beanName should be looked up
	 * @param beanName Name of the bean to inject.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> propertyRefFromContext(String propertyName, String fromContext, String beanName);

	/**
	 * Defines a bean to be injected into a specific property of the new bean.
	 *
	 * @param propertyName Name of the property.
	 * @param bean IBeanConfiguration of the bean to be injected.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> propertyRef(String propertyName, IBeanConfiguration bean);

	/**
	 * Defines a named bean to be injected into a matching property of the new bean.
	 *
	 * @see IBeanConfiguration#propertyRef(String, String)
	 *
	 * @param beanName Name of the bean to be injected.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> propertyRefs(String beanName);

	/**
	 * Defines multiple named beans to be injected into appropriate properties of the new bean.
	 *
	 * @see IBeanConfiguration#propertyRef(String, String)
	 *
	 * @param beanNames Names of the bean to be injected.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> propertyRefs(String... beanNames);

	/**
	 * Defines a bean to be injected into a matching property of the new bean.
	 *
	 * @param bean IBeanConfiguration of the bean to be injected.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> propertyRef(IBeanConfiguration bean);

	/**
	 * Sets a defined property of the new bean to a given value (primitive, entity,...).
	 *
	 * @param propertyName Name of the property.
	 * @param value Value to set.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> propertyValue(String propertyName, Object value);

	/**
	 * Defines a property of the new bean to be ignored by dependency injection and configuration
	 * property setter.
	 *
	 * @param propertyName Name of the property to ignore.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> ignoreProperties(String propertyName);

	/**
	 * Defines multiple properties of the new bean to be ignored by dependency injection and
	 * configuration property setter.
	 *
	 * @param propertyNames Names of the properties to ignore.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> ignoreProperties(String... propertyNames);

	/**
	 * Consider carefully using the method: Its purpose is to allow a raw access to the instantiated
	 * bean BEFORE its properties are initialized and before any post processors (AOP) has been
	 * applied
	 * 
	 * @return The raw bean instance. If called before {@link #finish()} it is not guaranteed what the
	 *         state of this returned instance is. It is strongly recommended to just use it as an
	 *         "object pointer" for more complex configuration scenarios till the IoC lifecycle is in
	 *         an initialized state
	 */
	V getInstance();
}
