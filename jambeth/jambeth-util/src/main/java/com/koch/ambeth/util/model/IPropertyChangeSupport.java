package com.koch.ambeth.util.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public interface IPropertyChangeSupport extends INotifyPropertyChanged
{
	/**
	 * Fires a property change event to listeners that have been registered to track updates of collection.
	 * 
	 * @param obj
	 * @param propertyName
	 * @param oldValue
	 * @param currentValue
	 */
	void firePropertyChange(Object obj, String propertyName, Object oldValue, Object currentValue);

	/**
	 * Fires a property change event to listeners that have been registered to track updates of collection.
	 * 
	 * @param evnt
	 *            the {@code NotifyCollectionChangedEvent} to be fired
	 */
	void firePropertyChange(PropertyChangeEvent evnt);

	/**
	 * Returns an array of all the listeners that were added to the NotifyCollectionChangedSupport object with addNotifyCollectionChangedListener().
	 * 
	 * @return all of the <code>NotifyCollectionChangedListeners</code> added or an empty array if no listeners have been added
	 */
	PropertyChangeListener[] getPropertyChangeListeners();
}
