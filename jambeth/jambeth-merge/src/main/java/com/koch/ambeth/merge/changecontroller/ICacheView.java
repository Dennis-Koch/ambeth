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
import java.util.Set;

import com.koch.ambeth.merge.incremental.IMergePipelineFinishHookExtendable;
import com.koch.ambeth.merge.model.IChangeContainer;

/**
 * This is a utility class that provides access to all new objects that should be merged with the
 * database.
 */
public interface ICacheView extends IMergePipelineFinishHookExtendable {

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
	 * Returns the current change container to any given new/current object describing the current
	 * transition step from "old" to "new". This is helpful if you need detailed information about the
	 * transition without the potential overhead to evaluate manually the changes on the given "old"
	 * and "new" objects.
	 *
	 * @param newOrOldObject The object to lookup its corresponding transitional step change
	 * @return The transitional step change handle
	 */
	IChangeContainer getChangeContainer(Object newObject);

	Set<String> getChangedMembers(Object newObject);

	<V> V getCustomState(Object key);

	<V> V setCustomState(Object key, Object value);

	/**
	 * Convenience method to add an item to a custom state key where the real value is a
	 * {@link java.util.List}. This reduces boilerplate code to lookup the value manually, if not
	 * exists create the list and add it and then adding the item to the list.<br>
	 * <code><br>
	 * Collection<Object> list = getCustomState(key);<br>
	 * if (list == null) {<br>
	 * &nbsp;list = new ArrayList<>();<br>
	 * &nbsp;setCustomState(key, list);<br>
	 * }<br>
	 * list.add(item);</code><br>
	 * <br>
	 *
	 * @param key The key checked with equals (not identity equals) within the custom state
	 * @param item An item to be added to the implicit collection bound to the specified key. The
	 *        collection is created on-demand
	 */
	void addCustomStateItem(Object key, Object item);

	/**
	 * Allows to register a custom hook which is executed before the flush phase of the current step.
	 * The implementation of the hook may work with the passed {@link ICacheView}, enrich it in any
	 * way and may even queue further hooks. All hooks will be processed in sequence before the "pre
	 * flush step" phase is finished and the ordinary "flush step" phase is entered
	 *
	 * @param mergeStepPreFlushHook
	 */
	void queuePreFlush(IMergeStepPreFlushHook mergeStepPreFlushHook);
}
