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

/**
 * Describes the action that caused a INotifyCollectionChanged.CollectionChanged event.
 */
public enum NotifyCollectionChangedAction {
	/** One or more items were added to the collection. */
	Add(0),

	/** One or more items were removed from the collection. */
	Remove(1),

	/** One or more items were replaced in the collection. */
	Replace(2),

	/** One or more items were moved within the collection. */
	Move(3),

	/** The content of the collection changed dramatically. */
	Reset(4);

	private final int value;

	NotifyCollectionChangedAction(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
