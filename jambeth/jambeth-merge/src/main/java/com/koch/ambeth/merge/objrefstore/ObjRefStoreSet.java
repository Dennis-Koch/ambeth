package com.koch.ambeth.merge.objrefstore;

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

import com.koch.ambeth.util.StringBuilderUtil;

/**
 * 66 percent faster compared to a normal HashMap with a Tuple2 (Composite-)Key as the Map-Key. This
 * is due to the fact that there is no need to instantiate Tuple2 Keys for put() or get()
 * operations. Of course the overall memory footprint is also the half compared to a normal map:
 * There is only the entry object with 2 key-fields compared to the entry object compared to 1
 * key-field which contains a Tuple2 Key instance
 *
 * @param <Key1>
 * @param <Key2>
 * @param
 */
public class ObjRefStoreSet {
	public static final int DEFAULT_INITIAL_CAPACITY = 16;

	public static final int MAXIMUM_CAPACITY = 1 << 30;

	public static final float DEFAULT_LOAD_FACTOR = 0.75f;

	protected final float loadFactor;

	protected int threshold;

	protected ObjRefStore[] table;

	protected int size;

	protected final IObjRefStoreEntryProvider objRefStoreEntryProvider;

	public ObjRefStoreSet(IObjRefStoreEntryProvider objRefStoreEntryProvider) {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, objRefStoreEntryProvider);
	}

	public ObjRefStoreSet(int initialCapacity, float loadFactor,
			IObjRefStoreEntryProvider objRefStoreEntryProvider) {
		this.loadFactor = loadFactor;
		this.objRefStoreEntryProvider = objRefStoreEntryProvider;

		if (initialCapacity < 0) {
			throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
		}
		if (initialCapacity > MAXIMUM_CAPACITY) {
			initialCapacity = MAXIMUM_CAPACITY;
		}
		if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
			throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
		}

		// Find a power of 2 >= initialCapacity
		int capacity = 1;
		while (capacity < initialCapacity) {
			capacity <<= 1;
		}

		threshold = (int) (capacity * loadFactor);
		table = createTable(capacity);
	}

	protected ObjRefStore[] createTable(final int capacity) {
		return new ObjRefStore[capacity];
	}

	protected int extractHash(final Class<?> entityType, final Object id, final byte idIndex) {
		return entityType.hashCode() ^ id.hashCode() ^ idIndex;
	}

	protected static int hash(int hash) {
		hash += ~(hash << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;
		return hash;
	}

	protected ObjRefStore addEntry(final Class<?> entityType, final Object id, final byte idIndex,
			final int bucketIndex) {
		ObjRefStore[] table = this.table;
		ObjRefStore e = table[bucketIndex];
		e = createEntry(entityType, id, idIndex, e);
		table[bucketIndex] = e;
		entryAdded(e);
		if (size() >= threshold) {
			resize(2 * table.length);
		}
		return e;
	}

	protected void resize(final int newCapacity) {
		final ObjRefStore[] oldTable = table;
		final int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY) {
			threshold = Integer.MAX_VALUE;
			return;
		}

		final ObjRefStore[] newTable = createTable(newCapacity);
		transfer(newTable);
		table = newTable;
		threshold = (int) (newCapacity * loadFactor);
	}

	public boolean containsKey(final Class<?> entityType, final byte idIndex, final Object id) {
		final int hash = hash(extractHash(entityType, id, idIndex));
		ObjRefStore[] table = this.table;
		final int i = hash & (table.length - 1);
		ObjRefStore entry = table[i];

		while (entry != null) {
			if (equalKeys(entityType, id, idIndex, entry)) {
				return true;
			}
			entry = entry.getNextEntry();
		}
		return false;
	}

	protected boolean equalKeys(final Class<?> entityType, final Object id, final byte idIndex,
			final ObjRefStore entry) {
		return entry.isEqualTo(entityType, idIndex, id);
	}

	public ObjRefStore put(final Class<?> entityType, final byte idIndex, final Object id) {
		final int hash = hash(extractHash(entityType, id, idIndex));
		ObjRefStore[] table = this.table;
		final int i = hash & (table.length - 1);

		ObjRefStore entry = table[i];
		while (entry != null) {
			if (equalKeys(entityType, id, idIndex, entry)) {
				return entry;
			}
			entry = entry.getNextEntry();
		}
		return addEntry(entityType, id, idIndex, i);
	}

	public ObjRefStore remove(final ObjRefStore objRefStore) {
		return removeEntryForKey(objRefStore.getRealType(), objRefStore.getIdNameIndex(),
				objRefStore.getId());
	}

	public ObjRefStore remove(final Class<?> entityType, final byte idIndex, final Object id) {
		return removeEntryForKey(entityType, idIndex, id);
	}

	protected final ObjRefStore removeEntryForKey(final Class<?> entityType, final byte idIndex,
			final Object id) {
		final int hash = hash(extractHash(entityType, id, idIndex));
		ObjRefStore[] table = this.table;
		final int i = hash & (table.length - 1);
		ObjRefStore entry = table[i];
		if (entry != null) {
			if (equalKeys(entityType, id, idIndex, entry)) {
				table[i] = entry.getNextEntry();
				entryRemoved(entry);
				return entry;
			}
			ObjRefStore prevEntry = entry;
			entry = entry.getNextEntry();
			while (entry != null) {
				if (equalKeys(entityType, id, idIndex, entry)) {
					prevEntry.setNextEntry(entry.getNextEntry());
					entryRemoved(entry);
					return entry;
				}
				prevEntry = entry;
				entry = entry.getNextEntry();
			}
		}
		return null;
	}

	public ObjRefStore get(final Class<?> entityType, final byte idIndex, final Object id) {
		final int hash = hash(extractHash(entityType, id, idIndex));
		ObjRefStore[] table = this.table;
		final int i = hash & (table.length - 1);
		ObjRefStore entry = table[i];
		while (entry != null) {
			if (equalKeys(entityType, id, idIndex, entry)) {
				return entry;
			}
			entry = entry.getNextEntry();
		}
		return null;
	}

	protected ObjRefStore createEntry(final Class<?> entityType, final Object id, final byte idIndex,
			final ObjRefStore nextEntry) {
		return objRefStoreEntryProvider.createObjRefStore(entityType, idIndex, id, nextEntry);
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	public final int size() {
		return size;
	}

	protected void entryAdded(final ObjRefStore e) {
		size++;
	}

	protected void entryRemoved(final ObjRefStore e) {
		size--;
	}

	protected void transfer(final ObjRefStore[] newTable) {
		final int newCapacityMinus1 = newTable.length - 1;
		final ObjRefStore[] table = this.table;

		for (int a = table.length; a-- > 0;) {
			ObjRefStore entry = table[a], next;
			while (entry != null) {
				next = entry.getNextEntry();
				int hash = hash(extractHash(entry.getRealType(), entry.getId(), entry.getIdNameIndex()));
				int i = hash & newCapacityMinus1;
				entry.setNextEntry(newTable[i]);
				newTable[i] = entry;
				entry = next;
			}
		}
	}

	public Object[] toArray() {
		int index = 0;
		Object[] targetArray = new Object[size()];
		ObjRefStore[] table = this.table;
		for (int a = table.length; a-- > 0;) {
			ObjRefStore entry = table[a];
			while (entry != null) {
				targetArray[index++] = entry;
				entry = entry.getNextEntry();
			}
		}
		return targetArray;
	}

	public void clear() {
		if (isEmpty()) {
			return;
		}
		final ObjRefStore[] table = this.table;

		for (int a = table.length; a-- > 0;) {
			ObjRefStore entry = table[a];
			if (entry != null) {
				table[a] = null;
				while (entry != null) {
					final ObjRefStore nextEntry = entry.getNextEntry();
					entryRemoved(entry);
					entry = nextEntry;
				}
			}
		}
	}

	public void toString(StringBuilder sb) {
		sb.append(size()).append(" items: [");
		boolean first = true;

		ObjRefStore[] table = this.table;
		for (int a = table.length; a-- > 0;) {
			ObjRefStore entry = table[a];
			while (entry != null) {
				if (first) {
					first = false;
				}
				else {
					sb.append(',');
				}
				StringBuilderUtil.appendPrintable(sb, entry);
				entry = entry.getNextEntry();
			}
		}
		sb.append(']');
	}
}
