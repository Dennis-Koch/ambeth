package de.osthus.ambeth.ioc.link;

/**
 * Last step of the fluent interface for a link configuration at runtime.
 * 
 * @see ILinkRuntimeExtendable
 */
public interface ILinkRuntimeWithOptional extends ILinkRuntimeOptional, ILinkRuntimeFinish
{
	/**
	 * Adds key(s) to the configuration the bean will be registered for.
	 * 
	 * @param arguments
	 *            Key(s) for the bean.
	 * @return This configuration.
	 */
	ILinkRuntimeOptional with(Object... arguments);
}
