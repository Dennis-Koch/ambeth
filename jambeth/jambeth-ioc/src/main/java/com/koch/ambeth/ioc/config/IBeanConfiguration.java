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

import java.util.List;

/**
 * Represents the IoC configuration of a bean instance.
 */
public interface IBeanConfiguration
{
	/**
	 * Getter for the Stacktrace where this instance has been declared
	 * 
	 * @return Stacktrace of declaration
	 */
	StackTraceElement[] getDeclarationStackTrace();

	/**
	 * Getter for the bean instance.
	 * 
	 * @return Bean instance.
	 */
	Object getInstance();

	/**
	 * Getter for the bean instance.
	 * 
	 * @param instanceType
	 *            Bean type.
	 * @return Bean instance.
	 */
	Object getInstance(Class<?> instanceType);

	/**
	 * Getter for the bean name (given or generated).
	 * 
	 * @return Bean name.
	 */
	String getName();

	/**
	 * Getter for the beans template name if any.
	 * 
	 * @return Template name if one was used, null otherwise.
	 */
	String getParentName();

	/**
	 * Getter for the bean type.
	 * 
	 * @return Type of the bean.
	 */
	Class<?> getBeanType();

	/**
	 * Getter for the configurations abstract flag. This defines the bean as real bean or template for other beans.
	 * 
	 * @return True if this is a template configuration, otherwise false.
	 */
	boolean isAbstract();

	/**
	 * A bean that was registered by class at start time is always registered with life cycle. Only if the bean was registered as an instance it may not be
	 * called on life cycle events.
	 * 
	 * @return True if the bean is registered with life cycle, otherwise false.
	 */
	boolean isWithLifecycle();

	/**
	 * Getter for the precedence level of this bean. By the precedence level the order of bean initializations can be changed. The initialization order of beans
	 * with the same precedence level is undefined, except for resolving dependencies.
	 * 
	 * @return Precedence level of this bean.
	 */
	PrecedenceType getPrecedence();

	/**
	 * Flag if this bean configuration is allowed to replace an existing configuration for the given bean name.
	 * 
	 * @return True if this configuration may override existing, false otherwise.
	 */
	boolean isOverridesExisting();

	/**
	 * Getter for the ignored property names. Those are properties to be ignored by the dependency injection.
	 * 
	 * @return List of property names to ignore.
	 */
	List<String> getIgnoredPropertyNames();

	/**
	 * Getter for the types this bean will be autowired to.
	 * 
	 * @return List of autowire target types.
	 */
	List<Class<?>> getAutowireableTypes();

	/**
	 * Getter for the property configurations for the bean.
	 * 
	 * @return List of property configurations.
	 */
	List<IPropertyConfiguration> getPropertyConfigurations();

	/**
	 * Setter for the precedence level of the bean.
	 * 
	 * @param precedenceType
	 *            Precedence level of this bean.
	 * @return This bean configuration.
	 */
	IBeanConfiguration precedence(PrecedenceType precedenceType);

	/**
	 * Sets this bean configuration to be a template for other bean configurations.
	 * 
	 * @return This bean configuration.
	 */
	IBeanConfiguration template();

	/**
	 * Setter for the name of the template bean configuration to use.
	 * 
	 * @param parentBeanTemplateName
	 *            Name of the template bean configuration to use.
	 * @return This bean configuration.
	 */
	IBeanConfiguration parent(String parentBeanTemplateName);

	/**
	 * Allows this bean configuration to replace another one for the same bean name.
	 * 
	 * @return This bean configuration.
	 */
	IBeanConfiguration overridesExisting();

	/**
	 * Adds one type to the types the bean will be autowired to.
	 * 
	 * @param typeToPublish
	 *            Autowire type for this bean.
	 * @return This bean configuration.
	 */
	IBeanConfiguration autowireable(Class<?> typeToPublish);

	/**
	 * Adds types to the types the bean will be autowired to.
	 * 
	 * @param typesToPublish
	 *            Autowire types for this bean.
	 * @return This bean configuration.
	 */
	IBeanConfiguration autowireable(Class<?>... typesToPublish);

	/**
	 * Specifies a property injection and references the bean to inject by its bean name.
	 * <p>
	 * The <tt>propertyRef()</tt> methods also allow <b>special references</b>.<br>
	 * - An asterisk/star (*) in front of the bean name ignores the current IoC context and directs the search to its parent context, even if a bean with the
	 * name exists in the current context.<br>
	 * - An ampersand (&) in front of the bean name requests the FactoryBean with this name. This is only useful if the bean in fact is an FactoryBean.
	 * 
	 * @param propertyName
	 *            Name of the target property.
	 * @param beanName
	 *            Name of the bean to inject.
	 * @return This bean configuration.
	 */
	IBeanConfiguration propertyRef(String propertyName, String beanName);

	/**
	 * Specifies a property injection and references the bean to inject by its bean configuration.
	 * 
	 * @param propertyName
	 *            Name of the target property.
	 * @param bean
	 *            Bean configuration of the bean to inject.
	 * @return This bean configuration.
	 */
	IBeanConfiguration propertyRef(String propertyName, IBeanConfiguration bean);

	/**
	 * Specifies a property injection and references the bean to inject by its bean context name and bean name (in the given context).
	 * <p>
	 * The <tt>propertyRef()</tt> methods also allow <b>special references</b>.<br>
	 * - An asterisk/star (*) in front of the bean name ignores the directly resolved IoC context and directs the search to its parent context, even if a bean
	 * with the name exists in the directly resolved IoC context.<br>
	 * - An ampersand (&) in front of the bean name requests the FactoryBean with this name. This is only useful if the bean in fact is an FactoryBean.
	 * 
	 * @param propertyName
	 *            Name of the target property.
	 * @param fromContext
	 *            Name of the bean context where the beanName should be looked up
	 * @param beanName
	 *            Name of the bean to inject.
	 * @return This bean configuration.
	 */
	IBeanConfiguration propertyRefFromContext(String propertyName, String fromContext, String beanName);

	/**
	 * Specifies a property injection and references the bean to inject by its name. The property will be chosen by the type of the specified bean to match the
	 * property type.
	 * 
	 * @see IBeanConfiguration#propertyRef(String, String)
	 * 
	 * @param beanName
	 *            Name of the bean to inject.
	 * @return This bean configuration.
	 */
	IBeanConfiguration propertyRefs(String beanName);

	/**
	 * Specifies multiple property injections and references the beans to inject by their names. The properties will be chosen by the type of the specified
	 * beans to match the property type.
	 * 
	 * @see IBeanConfiguration#propertyRef(String, String)
	 * 
	 * @param beanNames
	 *            Names of the beans to inject.
	 * @return This bean configuration.
	 */
	IBeanConfiguration propertyRefs(String... beanNames);

	/**
	 * Specifies a property injection and references the bean to inject by its bean configuration. The property will be chosen by the type of the specified bean
	 * to match the property type.
	 * 
	 * @param bean
	 *            Bean configuration of the bean to inject.
	 * @return This bean configuration.
	 */
	IBeanConfiguration propertyRef(IBeanConfiguration bean);

	/**
	 * Specifies multiple property injections and references the beans to inject by their bean configurations. The properties will be chosen by the type of the
	 * specified beans to match the property type.
	 * 
	 * @param bean
	 *            Bean configurations of the beans to inject.
	 * @return This bean configuration.
	 */
	IBeanConfiguration propertyRefs(IBeanConfiguration... beans);

	/**
	 * Specifies a property injection including the value to be injected. This value may be any bean, Object or primitive value matching the properties type.
	 * 
	 * @param propertyName
	 *            Name of the target property.
	 * @param value
	 *            Value to inject.
	 * @return This bean configuration.
	 */
	IBeanConfiguration propertyValue(String propertyName, Object value);

	/**
	 * Adds one name of a property to be ignored by dependency injection.
	 * 
	 * @param propertyName
	 *            Name of the property to be ignored.
	 * @return This bean configuration.
	 */
	IBeanConfiguration ignoreProperties(String propertyName);

	/**
	 * Adds names of properties to be ignored by dependency injection.
	 * 
	 * @param propertyNames
	 *            Names of properties to be ignored.
	 * @return This bean configuration.
	 */
	IBeanConfiguration ignoreProperties(String... propertyNames);
}
