package de.osthus.ambeth.changecontroller;

/**
 * Rules that want to work in a batch mode: that is "queue" necessary steps until the batch is "flushed"
 */
public interface IBatchAwareChangeControllerExtension<T> extends IChangeControllerExtension<T>
{
	/**
	 * Callback to notify the extension that a new batch sequence is started
	 */
	void queue(ICacheView cacheView);

	/**
	 * Callback to notify the extension that the previous batch sequence called with "queue" is now finished.
	 */
	void flush(ICacheView cacheView);

	/**
	 * Callback to notify the extension that the previous batch sequence called with "queue" is reverted (mostly due to an exception).
	 */
	void rollback(ICacheView cacheView);
}
