package de.osthus.ambeth.bundle;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

/**
 * Interface for bundle modules that define and configure the content of the IoC container for a specific bundle.
 */
public interface IBundleModule
{
	/**
	 * Called by the starting IoC container after the module has been instantiated to register the modules defining the bundle.
	 * 
	 * @param beanContextFactory
	 *            Starting context.
	 * @throws Throwable
	 */
	void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable;
}
