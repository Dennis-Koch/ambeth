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

import java.util.EventListener;

/**
 * Represents the method that handles the {@link INotifyCollectionChanged} collectionChanged event
 */
public interface INotifyCollectionChangedListener extends EventListener {
	/**
	 * This method gets called when an ObservableCollection is changed.
	 *
	 * @param evt A {@link NotifyCollectionChangedEvent} object describing the event source and the
	 *        collection that has changed.
	 */
	void collectionChanged(NotifyCollectionChangedEvent evt);
}
