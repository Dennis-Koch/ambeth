package de.osthus.ambeth.ioc.link;

/**
 * Last step of the fluent interface for a link configuration at runtime.
 * 
 * @see ILinkRuntimeExtendable
 */
public interface ILinkRuntimeOptional extends ILinkRuntimeFinish
{
	/**
	 * Sets the linking as optional. It may be omitted if the registry cannot be found.
	 * 
	 * @return This configuration.
	 */
	ILinkRuntimeFinish optional();
}
