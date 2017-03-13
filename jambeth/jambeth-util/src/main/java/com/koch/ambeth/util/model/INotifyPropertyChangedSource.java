package com.koch.ambeth.util.model;

import java.beans.PropertyChangeListener;

import com.koch.ambeth.util.collections.specialized.INotifyCollectionChangedListener;
import com.koch.ambeth.util.collections.specialized.PropertyChangeSupport;

public interface INotifyPropertyChangedSource
{
	PropertyChangeSupport getPropertyChangeSupport();

	PropertyChangeListener getParentChildEventHandler();

	INotifyCollectionChangedListener getCollectionEventHandler();

	void onPropertyChanged(String propertyName);

	void onPropertyChanged(String propertyName, Object oldValue, Object newValue);
}
