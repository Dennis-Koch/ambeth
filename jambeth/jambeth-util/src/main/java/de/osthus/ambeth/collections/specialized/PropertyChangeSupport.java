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

	private PropertyChangeListener[] listenersCopy = EMPTY_LISTENERS;

	public PropertyChangeSupport()
	{
		super(1);
	}

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
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
	 * {@inheritDoc}
	 */
	@Override
	public PropertyChangeListener[] getPropertyChangeListeners()
	{
		if (size() == 0)
		{
			return EMPTY_LISTENERS;
		}
		return toArray(PropertyChangeListener.class);
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
		firePropertyChange(new PropertyChangeEvent(obj, propertyName, oldValue, currentValue));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firePropertyChange(PropertyChangeEvent evnt)
	{
		for (PropertyChangeListener listener : listenersCopy)
		{
			listener.propertyChange(evnt);
		}
	}
}
