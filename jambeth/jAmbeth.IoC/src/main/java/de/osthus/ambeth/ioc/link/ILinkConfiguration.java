package de.osthus.ambeth.ioc.link;

/**
 * Interface for a finished but configurable link configuration.
 * 
 * @see ILinkExtendable
 */
public interface ILinkConfiguration
{
	/**
	 * Adds key(s) to the configuration the bean will be registered for.
	 * 
	 * @param arguments
	 *            Key(s) for the bean.
	 * @return This configuration.
	 */
	ILinkConfiguration with(Object... arguments);

	/**
	 * Sets the linking as optional. It may be omitted if the registry cannot be found.
	 * 
	 * @return This configuration.
	 */
	ILinkConfiguration optional();
}
