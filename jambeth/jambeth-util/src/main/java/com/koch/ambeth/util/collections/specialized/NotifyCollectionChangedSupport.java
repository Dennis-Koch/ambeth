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
 * This is a utility class that manages a list of listeners and dispatches {@link NotifyCollectionChangedEvent}s to them. You can use an instance of this class
 * as a member field of your collection and delegate these types of work to it. The {@link INotifyCollectionChangedListener} can be registered for the
 * collection
 */
public class NotifyCollectionChangedSupport extends ArrayList<INotifyCollectionChangedListener>
{
	private INotifyCollectionChangedListener[] listenersCopy = toArray(INotifyCollectionChangedListener.class);

	public NotifyCollectionChangedSupport()
	{
		super(1);
	}

	/**
	 * Add a NotifyCollectionChangedListener to the listener list. The listener is registered for all properties. The same listener object may be added more
	 * than once, and will be called as many times as it is added. If <code>listener</code> is null, no exception is thrown and no action is taken.
	 * 
	 * @param listener
	 *            The NotifyCollectionChangedListener to be added
	 */
	public void addNotifyCollectionChangedListener(INotifyCollectionChangedListener listener)
	{
		if (listener == null)
		{
			return;
		}
		if (!add(listener))
		{
			return;
		}
		listenersCopy = toArray(INotifyCollectionChangedListener.class);
	}

	/**
	 * Remove a NotifyCollectionChangedListener from the listener list. This removes a NotifyCollectionChangedListener that was registered for all properties.
	 * If <code>listener</code> was added more than once to the same event source, it will be notified one less time after being removed. If
	 * <code>listener</code> is null, or was never added, no exception is thrown and no action is taken.
	 * 
	 * @param listener
	 *            The NotifyCollectionChangedListener to be removed
	 */
	public void removeNotifyCollectionChangedListener(INotifyCollectionChangedListener listener)
	{
		if (listener == null)
		{
			return;
		}
		if (!remove(listener))
		{
			return;
		}
		listenersCopy = toArray(INotifyCollectionChangedListener.class);
	}

	/**
	 * Returns an array of all the listeners that were added to the NotifyCollectionChangedSupport object with addNotifyCollectionChangedListener().
	 * 
	 * @return all of the <code>NotifyCollectionChangedListeners</code> added or an empty array if no listeners have been added
	 */
	public INotifyCollectionChangedListener[] getNotifyCollectionChangedListeners()
	{
		return toArray(INotifyCollectionChangedListener.class);
	}

	/**
	 * Fires a property change event to listeners that have been registered to track updates of collection.
	 * 
	 * @param event
	 *            the {@code NotifyCollectionChangedEvent} to be fired
	 */
	public void fireNotifyCollectionChanged(NotifyCollectionChangedEvent event)
	{
		INotifyCollectionChangedListener[] listenersCopy = this.listenersCopy;
		for (INotifyCollectionChangedListener listener : listenersCopy)
		{
			listener.collectionChanged(event);
		}
	}
}
