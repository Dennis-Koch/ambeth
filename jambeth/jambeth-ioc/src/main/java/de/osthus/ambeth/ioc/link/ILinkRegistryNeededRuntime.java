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
	 * Identifies the registry to use by its autowired type.
	 * 
	 * @param autowiredRegistryClass
	 *            Autowired type of the registry bean.
	 * @return Next configuration step of the fluent interface.
	 */
	ILinkRuntimeWithOptional to(Class<?> autowiredRegistryClass);

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
	 * 
	 * @param eventDelegate
	 * @return Next configuration step of the fluent interface.
	 */
	ILinkRuntimeWithOptional to(Object registry, IEventDelegate<D> eventDelegate);

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
	 * @return Next configuration step of the fluent interface.
	 */
	ILinkRuntimeWithOptional to(Object registry, Class<?> registryClass);

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
	 * @return Next configuration step of the fluent interface.
	 */
	ILinkRuntimeWithOptional to(Object registry, String propertyName);
}