package de.osthus.ambeth.ioc.link;

/**
 * Second step of {@link ILinkRuntimeExtendable} fluent interface for linking beans to registries during runtime.
 * 
 * @param <D>
 *            Type of the bean to be linked.
 */
public interface ILinkRegistryNeededRuntime<D>
{
	/**
	 * Identifies the registry to use by its name.
	 * 
	 * @param registryBeanName
	 *            Name of the registry.
	 * @param eventDelegate
	 * @return Next configuration step of the fluent interface.
	 */
	ILinkRuntime to(String registryBeanName, IEventDelegate<D> eventDelegate);

	/**
	 * Identifies the registry to use by its name and specifies the registry class or interface to use.
	 * 
	 * @param registryBeanName
	 *            Name of the registry.
	 * @param registryClass
	 *            Class/Interface containing the registry methods.
	 * @return Next configuration step of the fluent interface.
	 */
	ILinkRuntime to(String registryBeanName, Class<?> registryClass);

	/**
	 * Identifies the registry to use by its name and specifies the registry method to use.
	 * 
	 * @param registryBeanName
	 *            Name of the registry.
	 * @param propertyName
	 *            Name of the registry method.
	 * @return Next configuration step of the fluent interface.
	 */
	ILinkRuntime to(String registryBeanName, String propertyName);

	/**
	 * Identifies the registry to use by its autowire interface.
	 * 
	 * @param autowiredRegistryClass
	 *            Autowire interface of the registry.
	 * @return Next configuration step of the fluent interface.
	 */
	ILinkRuntime to(Class<?> autowiredRegistryClass);

	/**
	 * Takes a registry instance to use for linking.
	 * 
	 * @param registry
	 *            Registry instance.
	 * @param eventDelegate
	 * @return Next configuration step of the fluent interface.
	 */
	ILinkRuntime to(Object registry, IEventDelegate<D> eventDelegate);

	/**
	 * Takes a registry instance to use for linking and specifies the registry class or interface to use.
	 * 
	 * @param registry
	 *            Registry instance.
	 * @param registryClass
	 *            Class/Interface containing the registry methods.
	 * @return Next configuration step of the fluent interface.
	 */
	ILinkRuntime to(Object registry, Class<?> registryClass);

	/**
	 * Takes a registry instance to use for linking and specifies the registry method to use.
	 * 
	 * @param registry
	 *            Registry instance.
	 * @param propertyName
	 *            Name of the registry method.
	 * @return Next configuration step of the fluent interface.
	 */
	ILinkRuntime to(Object registry, String propertyName);
}