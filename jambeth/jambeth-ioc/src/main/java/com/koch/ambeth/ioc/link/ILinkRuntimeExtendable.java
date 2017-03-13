package com.koch.ambeth.ioc.link;

import com.koch.ambeth.ioc.config.IBeanConfiguration;

/**
 * Interface for linking a bean (extension/plugin) to an extendable at runtime using a fluent interface.
 * 
 * @see ILinkExtendable
 */
public interface ILinkRuntimeExtendable
{
	/**
	 * Start linking a named bean.
	 * 
	 * @param listenerBeanName
	 *            Name of the bean to add to a registry.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededRuntime<?> link(String listenerBeanName);

	/**
	 * Start linking a bean identified by its {@link IBeanConfiguration}.
	 * 
	 * @param listenerBean
	 *            Identifier for the bean to add to a registry.
	 * @return Next step of the fluent interface.
	 */
	ILinkRegistryNeededRuntime<?> link(IBeanConfiguration listenerBean);

	/**
	 * Start linking a object or bean instance.
	 * 
	 * @param listener
	 *            Object or bean to add to a registry.
	 * @return Next step of the fluent interface.
	 */
	<D> ILinkRegistryNeededRuntime<D> link(D listener);
}
