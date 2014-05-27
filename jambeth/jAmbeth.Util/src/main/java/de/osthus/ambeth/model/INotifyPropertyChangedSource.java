package de.osthus.ambeth.model;

import java.beans.PropertyChangeListener;

import de.osthus.ambeth.collections.specialized.INotifyCollectionChangedListener;
import de.osthus.ambeth.collections.specialized.PropertyChangeSupport;

public interface INotifyPropertyChangedSource
{
	PropertyChangeSupport getPropertyChangeSupport();

	PropertyChangeListener getParentChildEventHandler();

	INotifyCollectionChangedListener getCollectionEventHandler();

	void onPropertyChanged(String propertyName);

	void onPropertyChanged(String propertyName, Object oldValue, Object newValue);
}
