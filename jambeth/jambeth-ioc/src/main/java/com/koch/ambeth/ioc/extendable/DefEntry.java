package com.koch.ambeth.ioc.extendable;

/*-
 * #%L
 * jambeth-ioc
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

import com.koch.ambeth.util.collections.IListElem;

public class DefEntry<V> implements IListElem<DefEntry<V>>, Comparable<DefEntry<V>> {
	protected IListElem<DefEntry<V>> prev, next;

	protected Object listHandle;

	public final V extension;

	public final Class<?> type;

	public final int distance;

	public DefEntry(V extension, Class<?> type, int distance) {
		this.extension = extension;
		this.type = type;
		this.distance = distance;
	}

	@Override
	public Object getListHandle() {
		return listHandle;
	}

	@Override
	public void setListHandle(Object listHandle) {
		this.listHandle = listHandle;
	}

	@Override
	public IListElem<DefEntry<V>> getPrev() {
		return prev;
	}

	@Override
	public void setPrev(IListElem<DefEntry<V>> prev) {
		this.prev = prev;
	}

	@Override
	public IListElem<DefEntry<V>> getNext() {
		return next;
	}

	@Override
	public void setNext(IListElem<DefEntry<V>> next) {
		this.next = next;
	}

	@Override
	public DefEntry<V> getElemValue() {
		return this;
	}

	@Override
	public void setElemValue(DefEntry<V> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(DefEntry<V> o) {
		if (o.distance > distance) {
			return 1;
		}
		if (o.distance == distance) {
			return 0;
		}
		return -1;
	}
}
