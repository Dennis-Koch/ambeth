package de.osthus.ambeth.ioc.link;

/**
 * Second step of {@link ILinkExtendable} fluent interface for linking beans to registries.
 * 
 * @param <D>
 *            Type of the bean to be linked.
 */
public interface ILinkRegistryNeededConfiguration<D>
{
	/**
	 * Identifies the registry to use by its autowired type.
	 * 
	 * @param autowiredRegistryClass
	 *            Autowired type of the registry.
	 * @return Optional next configuration step of the fluent interface.
	 */
	ILinkConfigWithOptional to(Class<?> autowiredRegistryClass);

	/**
	 * Takes a registry instance to use for linking.
	 * 
	 * @param registry
	 *            One of:<br/>
	 *            <p>
	 *            <ul>
	 *            <li>instance of the registry bean
	 *            <li>a string (referring to the name of the registry bean)
	 *            <li>a class (referring to the autowired type of the registry bean)
	 *            </ul>
	 *            <p>
	 * @param eventDelegate
	 * @return Optional next configuration step of the fluent interface.
	 */
	ILinkConfigWithOptional to(Object registry, IEventDelegate<D> eventDelegate);

	/**
	 * Takes a registry instance to use for linking and specifies the registry class or interface to use.
	 * 
	 * @param registry
	 *            One of:<br/>
	 *            <p>
	 *            <ul>
	 *            <li>instance of the registry bean
	 *            <li>a string (referring to the name of the registry bean)
	 *            <li>a class (referring to the autowired type of the registry bean)
	 *            </ul>
	 *            <p>
	 * @param registryClass
	 *            Class/Interface containing the registry methods.
	 * @return Optional next configuration step of the fluent interface.
	 */
	ILinkConfigWithOptional to(Object registry, Class<?> registryClass);

	/**
	 * Takes a registry instance to use for linking and specifies the registry method to use.
	 * 
	 * @param registry
	 *            One of:<br/>
	 *            <p>
	 *            <ul>
	 *            <li>instance of the registry bean
	 *            <li>a string (referring to the name of the registry bean)
	 *            <li>a class (referring to the autowired type of the registry bean)
	 *            </ul>
	 *            <p>
	 * @param propertyName
	 *            Name of the registry method.
	 * @return Optional next configuration step of the fluent interface.
	 */
	ILinkConfigWithOptional to(Object registry, String propertyName);
}
