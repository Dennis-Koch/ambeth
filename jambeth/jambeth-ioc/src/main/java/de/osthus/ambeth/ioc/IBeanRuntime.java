package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.config.IBeanConfiguration;

/**
 * Fluent interface to configure a new bean instance in a running jAmbeth IoC context.
 * 
 * @see IServiceContext#registerAnonymousBean(Class)
 * 
 * @param <V>
 *            Type of the new bean instance.
 */
public interface IBeanRuntime<V>
{
	/**
	 * Finish the bean configuration and add the new bean instance to the context.
	 * 
	 * @return New bean instance.
	 */
	V finish();

	/**
	 * Set the new bean to inherit the configuration from an abstract parent bean configuration.
	 * 
	 * @param parentBeanTemplateName
	 *            Given name of the parent bean configuration.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> parent(String parentBeanTemplateName);

	/**
	 * Defines a named bean to be injected into a specific property of the new bean.
	 * 
	 * @see IBeanConfiguration#propertyRef(String, String)
	 * 
	 * @param propertyName
	 *            Name of the property.
	 * @param beanName
	 *            Name of the bean to be injected.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> propertyRef(String propertyName, String beanName);

	/**
	 * Defines a bean to be injected into a specific property of the new bean.
	 * 
	 * @param propertyName
	 *            Name of the property.
	 * @param bean
	 *            IBeanConfiguration of the bean to be injected.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> propertyRef(String propertyName, IBeanConfiguration bean);

	/**
	 * Defines a named bean to be injected into a matching property of the new bean.
	 * 
	 * @see IBeanConfiguration#propertyRef(String, String)
	 * 
	 * @param beanName
	 *            Name of the bean to be injected.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> propertyRefs(String beanName);

	/**
	 * Defines multiple named beans to be injected into appropriate properties of the new bean.
	 * 
	 * @see IBeanConfiguration#propertyRef(String, String)
	 * 
	 * @param beanNames
	 *            Names of the bean to be injected.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> propertyRefs(String... beanNames);

	/**
	 * Defines a bean to be injected into a matching property of the new bean.
	 * 
	 * @param bean
	 *            IBeanConfiguration of the bean to be injected.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> propertyRef(IBeanConfiguration bean);

	/**
	 * Sets a defined property of the new bean to a given value (primitive, entity,...).
	 * 
	 * @param propertyName
	 *            Name of the property.
	 * @param value
	 *            Value to set.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> propertyValue(String propertyName, Object value);

	/**
	 * Defines a property of the new bean to be ignored by dependency injection and configuration property setter.
	 * 
	 * @param propertyName
	 *            Name of the property to ignore.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> ignoreProperties(String propertyName);

	/**
	 * Defines multiple properties of the new bean to be ignored by dependency injection and configuration property setter.
	 * 
	 * @param propertyNames
	 *            Names of the properties to ignore.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	IBeanRuntime<V> ignoreProperties(String... propertyNames);
}
