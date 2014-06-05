package de.osthus.ambeth.collections.specialized;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.model.INotifyPropertyChanged;

/**
 * This is a utility class that manages a list of listeners and dispatches {@link NotifyCollectionChangedEvent}s to them. You can use an instance of this class
 * as a member field of your collection and delegate these types of work to it. The {@link INotifyCollectionChangedListener} can be registered for the
 * collection
 */
public class PropertyChangeSupport extends ArrayList<PropertyChangeListener> implements INotifyPropertyChanged
{
	private static final PropertyChangeListener[] EMPTY_LISTENERS = new PropertyChangeListener[0];

	private PropertyChangeListener[] listenersCopy = EMPTY_LISTENERS;

	public PropertyChangeSupport()
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
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		if (listener == null)
		{
			return;
		}
		if (!add(listener))
		{
			return;
		}
		listenersCopy = toArray(PropertyChangeListener.class);
	}

	/**
	 * Remove a NotifyCollectionChangedListener from the listener list. This removes a NotifyCollectionChangedListener that was registered for all properties.
	 * If <code>listener</code> was added more than once to the same event source, it will be notified one less time after being removed. If
	 * <code>listener</code> is null, or was never added, no exception is thrown and no action is taken.
	 * 
	 * @param listener
	 *            The NotifyCollectionChangedListener to be removed
	 */
	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		if (listener == null)
		{
			return;
		}
		if (!remove(listener))
		{
			return;
		}
		if (size() == 0)
		{
			listenersCopy = EMPTY_LISTENERS;
		}
		else
		{
			listenersCopy = toArray(PropertyChangeListener.class);
		}
	}

	/**
	 * Returns an array of all the listeners that were added to the NotifyCollectionChangedSupport object with addNotifyCollectionChangedListener().
	 * 
	 * @return all of the <code>NotifyCollectionChangedListeners</code> added or an empty array if no listeners have been added
	 */
	public PropertyChangeListener[] getPropertyChangeListeners()
	{
		if (size() == 0)
		{
			return EMPTY_LISTENERS;
		}
		return toArray(PropertyChangeListener.class);
	}

	/**
	 * Fires a property change event to listeners that have been registered to track updates of collection.
	 * 
	 * @param event
	 *            the {@code NotifyCollectionChangedEvent} to be fired
	 */
	public void firePropertyChange(Object obj, String propertyName, Object oldValue, Object currentValue)
	{
		PropertyChangeListener[] listenersCopy = this.listenersCopy;
		if (listenersCopy.length == 0)
		{
			return;
		}
		firePropertyChange(new PropertyChangeEvent(obj, propertyName, oldValue, currentValue));
	}

	public void firePropertyChange(PropertyChangeEvent evnt)
	{
		for (PropertyChangeListener listener : listenersCopy)
		{
			listener.propertyChange(evnt);
		}
	}
}
