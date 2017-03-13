package com.koch.ambeth.util.collections.specialized;

import java.util.EventListener;

/**
 * Represents the method that handles the {@link INotifyCollectionChanged} collectionChanged event
 */
public interface INotifyCollectionChangedListener extends EventListener
{
	/**
	 * This method gets called when an ObservableCollection is changed.
	 * 
	 * @param evt
	 *            A {@link NotifyCollectionChangedEvent} object describing the event source and the collection that has changed.
	 */
	void collectionChanged(NotifyCollectionChangedEvent evt);
}
