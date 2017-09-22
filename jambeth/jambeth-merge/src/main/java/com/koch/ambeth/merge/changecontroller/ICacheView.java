package com.koch.ambeth.merge.changecontroller;

/*-
 * #%L
 * jambeth-merge
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

import java.util.Collection;

import com.koch.ambeth.merge.incremental.IMergeProcessFinishListenerExtendable;
import com.koch.ambeth.merge.model.IChangeContainer;

/**
 * This is a utility class that provides access to all new objects that should be merged with the
 * database.
 */
public interface ICacheView extends IMergeProcessFinishListenerExtendable {

	/**
	 * Returns a list of new objects that have the given interface as type.
	 *
	 * @param clazz The interface that the object should implemented by the objects
	 * @return a list of new objects that implement the interface, never <code>null</code>
	 */
	<T> Collection<T> getNewObjectsOfClass(Class<T> clazz);

	/**
	 * Returns a list of new objects that have the given interface as type.
	 *
	 * @param clazz The interface that the object should implemented by the objects
	 * @return a list of new objects that implement the interface, never <code>null</code>
	 */
	<T> Collection<T> getOldObjectsOfClass(Class<T> clazz);

	/**
	 * Returns the current change container to any given new or old object describing the current
	 * transition step from "old" to "new". This is helpful if you need detailed information about the
	 * transition without the potential overhead to evaluate manually the changes on the given "old"
	 * and "new" objects.
	 *
	 * @param newOrOldObject The object to lookup its corresponding transitional step change
	 * @return The transitional step change handle
	 */
	IChangeContainer getChangeContainer(Object newOrOldObject);

	<V> V getCustomState(Object key);

	<V> V setCustomState(Object key, Object value);

	void queuePreFlush(IMergeStepPreFlushListener mergeStepPreFlushListener);
}
