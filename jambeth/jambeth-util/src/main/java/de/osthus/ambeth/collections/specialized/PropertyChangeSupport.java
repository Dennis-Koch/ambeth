package de.osthus.ambeth.collections.specialized;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.model.IPropertyChangeSupport;

/**
 * This is a utility class that manages a list of listeners and dispatches {@link NotifyCollectionChangedEvent}s to them. You can use an instance of this class
 * as a member field of your collection and delegate these types of work to it. The {@link INotifyCollectionChangedListener} can be registered for the
 * collection
 */
public class PropertyChangeSupport extends ArrayList<PropertyChangeListener> implements IPropertyChangeSupport
{
	private static final PropertyChangeListener[] EMPTY_LISTENERS = new PropertyChangeListener[0];

	private volatile PropertyChangeListener[] listenersCopy = EMPTY_LISTENERS;

	public PropertyChangeSupport()
	{
		super(1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void addPropertyChangeListener(PropertyChangeListener listener)
	{
		if (listener == null)
		{
			return;
		}
		if (!add(listener))
		{
			return;
		}
		listenersCopy = toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void removePropertyChangeListener(PropertyChangeListener listener)
	{
		if (listener == null)
		{
			return;
		}
		if (!remove(listener))
		{
			return;
		}
		listenersCopy = toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyChangeListener[] getPropertyChangeListeners()
	{
		return toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firePropertyChange(Object obj, String propertyName, Object oldValue, Object currentValue)
	{
		PropertyChangeListener[] listenersCopy = this.listenersCopy;
		if (listenersCopy.length == 0)
		{
			return;
		}
		PropertyChangeEvent evnt = new PropertyChangeEvent(obj, propertyName, oldValue, currentValue);
		for (PropertyChangeListener listener : listenersCopy)
		{
			listener.propertyChange(evnt);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firePropertyChange(PropertyChangeEvent evnt)
	{
		PropertyChangeListener[] listenersCopy = this.listenersCopy;
		for (PropertyChangeListener listener : listenersCopy)
		{
			listener.propertyChange(evnt);
		}
	}

	@Override
	public synchronized PropertyChangeListener[] toArray()
	{
		if (size() == 0)
		{
			return EMPTY_LISTENERS;
		}
		// necessary to lock the otherwise not thread-safe toArray() operation
		return toArray(new PropertyChangeListener[size()]);
	}
}
