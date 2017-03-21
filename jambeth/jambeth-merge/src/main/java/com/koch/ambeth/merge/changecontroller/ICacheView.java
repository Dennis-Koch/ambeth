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

import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;

/**
 * This is a utility class that provides access to all new objects that should be merged with the database.
 */
public interface ICacheView
{

	/**
	 * Returns a list of new objects that have the given interface as type.
	 * 
	 * @param clazz
	 *            The interface that the object should implemented by the objects
	 * @return a list of new objects that implement the interface, never <code>null</code>
	 */
	<T> Collection<T> getNewObjectsOfClass(Class<T> clazz);

	/**
	 * Returns a list of new objects that have the given interface as type.
	 * 
	 * @param clazz
	 *            The interface that the object should implemented by the objects
	 * @return a list of new objects that implement the interface, never <code>null</code>
	 */
	<T> Collection<T> getOldObjectsOfClass(Class<T> clazz);

	Object getCustomState(Object key);

	void setCustomState(Object key, Object value);

	void queueRunnable(IBackgroundWorkerDelegate runnable);

}
