package com.koch.ambeth.util.collections;

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

import java.util.Iterator;

public class Tuple3KeyIterator<Key1, Key2, Key3, V>
		implements Iterator<Tuple3KeyEntry<Key1, Key2, Key3, V>> {
	protected Tuple3KeyEntry<Key1, Key2, Key3, V> currEntry, nextEntry;

	protected int index;

	protected final Tuple3KeyEntry<Key1, Key2, Key3, V>[] table;

	private final AbstractTuple3KeyHashMap<Key1, Key2, Key3, V> hashMap;

	private final boolean removeAllowed;

	public Tuple3KeyIterator(AbstractTuple3KeyHashMap<Key1, Key2, Key3, V> hashMap,
			Tuple3KeyEntry<Key1, Key2, Key3, V>[] table, boolean removeAllowed) {
		this.hashMap = hashMap;
		this.table = table;
		this.removeAllowed = removeAllowed;
		nextEntry = getNextBucketFromIndex(table.length);
	}

	protected Tuple3KeyEntry<Key1, Key2, Key3, V> getNextBucketFromIndex(int index) {
		Tuple3KeyEntry<Key1, Key2, Key3, V>[] table = this.table;
		while (index-- > 0) {
			Tuple3KeyEntry<Key1, Key2, Key3, V> entry = table[index];
			if (entry != null) {
				this.index = index;
				return entry;
			}
		}
		return null;
	}

	@Override
	public boolean hasNext() {
		return nextEntry != null;
	}

	@Override
	public Tuple3KeyEntry<Key1, Key2, Key3, V> next() {
		Tuple3KeyEntry<Key1, Key2, Key3, V> currEntry = nextEntry;
		Tuple3KeyEntry<Key1, Key2, Key3, V> nextEntry =
				currEntry != null ? currEntry.getNextEntry() : null;
		if (nextEntry != null) {
			this.nextEntry = nextEntry;
		}
		else {
			this.nextEntry = getNextBucketFromIndex(index);
		}
		return currEntry;
	}

	@Override
	public void remove() {
		if (!removeAllowed) {
			throw new UnsupportedOperationException();
		}
		hashMap.remove(currEntry.getKey1(), currEntry.getKey2(), currEntry.getKey3());
		currEntry = currEntry.getNextEntry();
	}
}
