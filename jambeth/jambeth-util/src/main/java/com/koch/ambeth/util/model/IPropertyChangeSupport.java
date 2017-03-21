package com.koch.ambeth.util.model;

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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public interface IPropertyChangeSupport extends INotifyPropertyChanged {
	/**
	 * Fires a property change event to listeners that have been registered to track updates of
	 * collection.
	 *
	 * @param obj
	 * @param propertyName
	 * @param oldValue
	 * @param currentValue
	 */
	void firePropertyChange(Object obj, String propertyName, Object oldValue, Object currentValue);

	/**
	 * Fires a property change event to listeners that have been registered to track updates of
	 * collection.
	 *
	 * @param evnt the {@code NotifyCollectionChangedEvent} to be fired
	 */
	void firePropertyChange(PropertyChangeEvent evnt);

	/**
	 * Returns an array of all the listeners that were added to the NotifyCollectionChangedSupport
	 * object with addNotifyCollectionChangedListener().
	 *
	 * @return all of the <code>NotifyCollectionChangedListeners</code> added or an empty array if no
	 *         listeners have been added
	 */
	PropertyChangeListener[] getPropertyChangeListeners();
}
