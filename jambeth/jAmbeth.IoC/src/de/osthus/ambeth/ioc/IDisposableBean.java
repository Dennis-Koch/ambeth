package de.osthus.ambeth.ioc;

/**
 * Interface for beans that need to clean-up internals on context shutdown.
 */
public interface IDisposableBean
{
	/**
	 * Method to be called on context shutdown.
	 * 
	 * @throws Throwable
	 *             Any exception that may occur during clean-up.
	 */
	void destroy() throws Throwable;
}
