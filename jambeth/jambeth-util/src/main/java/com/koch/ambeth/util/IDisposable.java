package com.koch.ambeth.util;

/**
 * Interface for objects that need to clean-up internals immediately when they are no longer in use. Some clean-ups should not wait until garbage collection.
 */
public interface IDisposable
{
	/**
	 * Method to be called when the object ist no longer in use.
	 */
	void dispose();
}
