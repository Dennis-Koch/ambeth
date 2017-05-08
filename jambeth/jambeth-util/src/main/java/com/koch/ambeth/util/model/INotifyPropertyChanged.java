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

import java.beans.PropertyChangeListener;

/**
 * A "PropertyChange" event gets fired whenever a bean changes a "bound" property. You can register
 * a PropertyChangeListener with a source bean so as to be notified of any bound property updates.
 */
public interface INotifyPropertyChanged {
	/**
	 * Registers a PropertyChangeListener to be notified of any bound property updates.
	 *
	 * @param listener The Listener to be registered
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Unregisters a PropertyChangeListener that has been registered before to be notified of any
	 * bound property updates.
	 *
	 * @param listener The Listener to be unregistered
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);
}
