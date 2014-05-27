package de.osthus.ambeth.collections.specialized;

/**
 * Notifies listeners of dynamic changes, such as when items get added and removed or the whole list is refreshed
 */
public interface INotifyCollectionChanged
{
	/**
	 * Registers a {@link INotifyCollectionChangedListener} to be notified of any collection updates.
	 * 
	 * @param listener
	 *            The Listener to be registered
	 */
	void addNotifyCollectionChangedListener(INotifyCollectionChangedListener listener);

	/**
	 * Unregisters a {@link INotifyCollectionChangedListener} that has been registered before to be notified of any collection updates.
	 * 
	 * @param listener
	 *            The Listener to be unregistered
	 */
	void removeNotifyCollectionChangedListener(INotifyCollectionChangedListener listener);
}
