package com.koch.ambeth.util.collections.specialized;

/*-
 * #%L
 * jambeth-util
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.util.collections.ArrayList;

/**
 * This is a utility class that manages a list of listeners and dispatches
 * {@link NotifyCollectionChangedEvent}s to them. You can use an instance of this class as a member
 * field of your collection and delegate these types of work to it. The
 * {@link INotifyCollectionChangedListener} can be registered for the collection
 */
public class CollectionChangeSupport extends ArrayList<INotifyCollectionChangedListener> {
	private static final INotifyCollectionChangedListener[] EMPTY_LISTENERS =
			new INotifyCollectionChangedListener[0];

	private volatile INotifyCollectionChangedListener[] listenersCopy = EMPTY_LISTENERS;

	public CollectionChangeSupport() {
		super(1);
	}

	/**
	 * Add a NotifyCollectionChangedListener to the listener list. The listener is registered for all
	 * properties. The same listener object may be added more than once, and will be called as many
	 * times as it is added. If <code>listener</code> is null, no exception is thrown and no action is
	 * taken.
	 *
	 * @param listener The NotifyCollectionChangedListener to be added
	 */
	public synchronized void addNotifyCollectionChangedListener(
			INotifyCollectionChangedListener listener) {
		if (listener == null) {
			return;
		}
		if (!add(listener)) {
			return;
		}
		listenersCopy = null;
	}

	/**
	 * Remove a NotifyCollectionChangedListener from the listener list. This removes a
	 * NotifyCollectionChangedListener that was registered for all properties. If
	 * <code>listener</code> was added more than once to the same event source, it will be notified
	 * one less time after being removed. If <code>listener</code> is null, or was never added, no
	 * exception is thrown and no action is taken.
	 *
	 * @param listener The NotifyCollectionChangedListener to be removed
	 */
	public synchronized void removeNotifyCollectionChangedListener(
			INotifyCollectionChangedListener listener) {
		if (listener == null) {
			return;
		}
		if (!remove(listener)) {
			return;
		}
		listenersCopy = null;
	}

	/**
	 * Returns an array of all the listeners that were added to the NotifyCollectionChangedSupport
	 * object with addNotifyCollectionChangedListener().
	 *
	 * @return all of the <code>NotifyCollectionChangedListeners</code> added or an empty array if no
	 *         listeners have been added
	 */
	public INotifyCollectionChangedListener[] getCollectionChangeListeners() {
		return toArray();
	}

	public INotifyCollectionChangedListener[] getCollectionChangeListenersShared() {
		INotifyCollectionChangedListener[] listenersCopy = this.listenersCopy;
		if (listenersCopy != null) {
			return listenersCopy;
		}
		listenersCopy = getCollectionChangeListeners();
		this.listenersCopy = listenersCopy;
		return listenersCopy;
	}

	/**
	 * Fires a property change event to listeners that have been registered to track updates of
	 * collection.
	 *
	 * @param event the {@code NotifyCollectionChangedEvent} to be fired
	 */
	public void fireCollectionChange(NotifyCollectionChangedEvent event) {
		for (INotifyCollectionChangedListener listener : getCollectionChangeListenersShared()) {
			listener.collectionChanged(event);
		}
	}

	@Override
	public synchronized INotifyCollectionChangedListener[] toArray() {
		if (size() == 0) {
			return EMPTY_LISTENERS;
		}
		// necessary to lock the otherwise not thread-safe toArray() operation
		return toArray(new INotifyCollectionChangedListener[size()]);
	}
}
