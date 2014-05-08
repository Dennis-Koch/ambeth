package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

/**
 * Interface for initializing modules that define and configure the content of the IoC container.
 * 
 * @see {@wiki osthus Verwendung}
 */
public interface IInitializingModule
{
	/**
	 * Called by the starting IoC container after the module has been instantiated and injected with required beans and properties. Implement this method to
	 * register and link beans during application startup.
	 * 
	 * @param beanContextFactory
	 *            Starting context.
	 * @throws Throwable
	 */
	void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable;
}
