package de.osthus.ambeth.ioc.link;

/**
 * Last step of the fluent interface for a link configuration at runtime.
 * 
 * @see ILinkRuntimeExtendable
 */
public interface ILinkRuntime
{
	/**
	 * Adds key(s) to the configuration the bean will be registered for.
	 * 
	 * @param arguments
	 *            Key(s) for the bean.
	 * @return This configuration.
	 */
	ILinkRuntime with(Object... arguments);

	/**
	 * Sets the linking as optional. It may be omitted if the registry cannot be found.
	 * 
	 * @return This configuration.
	 */
	ILinkRuntime optional();

	/**
	 * Finishes the link configuration and executes it.
	 */
	void finishLink();
}
