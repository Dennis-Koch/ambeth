package de.osthus.ambeth.ioc.link;

/**
 * Interface for a finished but configurable link configuration.
 * 
 * @see ILinkExtendable
 */
public interface ILinkConfigOptional
{
	/**
	 * Sets the linking as optional. It may be omitted if the registry cannot be found.
	 * 
	 * @return This configuration.
	 */
	void optional();
}
