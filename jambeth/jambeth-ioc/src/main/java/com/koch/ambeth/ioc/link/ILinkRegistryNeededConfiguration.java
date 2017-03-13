package com.koch.ambeth.ioc.link;

import com.koch.ambeth.ioc.IServiceContext;

/**
 * Second step of {@link ILinkExtendable} fluent interface for linking beans to registries.
 * 
 * @param <D>
 *            Type of the bean to be linked.
 */
public interface ILinkRegistryNeededConfiguration<D>
{
	/**
	 * Defines that the registry - which is referred to in one of the {@link #to(Class)} overloads - has to be looked up in a specific foreign context. The
	 * specific {@link #toContext(String)} overload here assumes a bean in the current container hierarchy to be found and that the referred bean is by itself
	 * an instance of {@link IServiceContext}. The lifecycle of the overall resulting link operation in therefore bound to 2 lifecycles: The one of the current
	 * IOC container and the one of the referred IOC container, which may be in practice fully independent. The link will be unlinked if either of those
	 * contexts gets disposed.
	 * 
	 * @param nameOfBeanContext
	 *            The name of a bean. The bean must be an instance of {@link IServiceContext} and is the target to look for the registry bean of the current
	 *            link operation
	 * @return Optional next configuration step of the fluent interface.
	 */
	ILinkRegistryNeededConfiguration<D> toContext(String nameOfBeanContext);

	/**
	 * Defines that the registry - which is referred to in one of the {@link #to(Class)} overloads - has to be looked up in a specific foreign context. The
	 * specific {@link #toContext(IServiceContext))} overload here expects the explicit instance of the target container of the registry bean. The lifecycle of
	 * the overall resulting link operation in therefore bound to 2 lifecycles: The one of the current IOC container and the one of the referred IOC container,
	 * which may be in practice fully independent. The link will be unlinked if either of those contexts gets disposed.
	 * 
	 * @param beanContext
	 *            The explicit instance of the container to look for the registry bean of the current link operation
	 * @return Optional next configuration step of the fluent interface.
	 */
	ILinkRegistryNeededConfiguration<D> toContext(IServiceContext beanContext);

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
