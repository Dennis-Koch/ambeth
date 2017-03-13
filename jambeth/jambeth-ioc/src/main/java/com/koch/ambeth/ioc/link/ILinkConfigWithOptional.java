package com.koch.ambeth.ioc.link;

/**
 * Interface for a finished but configurable link configuration.
 * 
 * @see ILinkExtendable
 */
public interface ILinkConfigWithOptional extends ILinkConfigOptional
{
	/**
	 * Adds key(s) to the configuration the bean will be registered for.
	 * 
	 * @param arguments
	 *            Key(s) for the bean.
	 * @return This configuration.
	 */
	ILinkConfigOptional with(Object... arguments);
}
