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

import java.util.Map.Entry;

public class MapIterator<WrappedK, K, V> extends AbstractIterator<Entry<K, V>> {
	protected IMapEntry<K, V> currEntry, nextEntry;

	protected int index;

	protected final IMapEntry<K, V>[] table;

	private final AbstractHashMap<WrappedK, K, V> hashMap;

	public MapIterator(AbstractHashMap<WrappedK, K, V> hashMap, IMapEntry<K, V>[] table,
			boolean removeAllowed) {
		super(removeAllowed);
		this.hashMap = hashMap;
		this.table = table;
		nextEntry = getNextBucketFromIndex(table.length);
	}

	protected IMapEntry<K, V> getNextBucketFromIndex(int index) {
		IMapEntry<K, V>[] table = this.table;
		while (index-- > 0) {
			IMapEntry<K, V> entry = table[index];
			while (entry != null && !entry.isValid()) {
				entry = entry.getNextEntry();
			}
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
	public Entry<K, V> next() {
		currEntry = nextEntry;
		IMapEntry<K, V> nextEntry = currEntry != null ? currEntry.getNextEntry() : null;
		while (nextEntry != null && !nextEntry.isValid()) {
			nextEntry = nextEntry.getNextEntry();
		}
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
		hashMap.remove(currEntry.getKey());
		currEntry = null;
	}
}
