package com.koch.ambeth.ioc.link;

/**
 * Last step of the fluent interface for a link configuration at runtime.
 * 
 * @see ILinkRuntimeExtendable
 */
public interface ILinkRuntimeFinish
{
	/**
	 * Finishes the link configuration and executes it.
	 */
	void finishLink();
}
