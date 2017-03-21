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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.util.Iterator;

import com.koch.ambeth.util.EqualsUtil;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;

public abstract class AbstractTuple4KeyHashMap<Key1, Key2, Key3, Key4, V> implements IPrintable, Iterable<Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>>,
		Externalizable
{
	public static final int DEFAULT_INITIAL_CAPACITY = 16;

	public static final int MAXIMUM_CAPACITY = 1 << 30;

	public static final float DEFAULT_LOAD_FACTOR = 0.75f;

	protected final float loadFactor;

	protected int threshold;

	protected Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table;

	public AbstractTuple4KeyHashMap(int initialCapacity, final float loadFactor)
	{
		this.loadFactor = loadFactor;

		if (initialCapacity < 0)
		{
			throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
		}
		if (initialCapacity > MAXIMUM_CAPACITY)
		{
			initialCapacity = MAXIMUM_CAPACITY;
		}
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
		{
			throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
		}

		// Find a power of 2 >= initialCapacity
		int capacity = 1;
		while (capacity < initialCapacity)
		{
			capacity <<= 1;
		}

		threshold = (int) (capacity * loadFactor);
		table = createTable(capacity);

		init();
	}

	@SuppressWarnings("unchecked")
	protected Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] createTable(final int capacity)
	{
		return (Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[]) Array.newInstance(Tuple4KeyEntry.class, capacity);
	}

	protected void init()
	{

	}

	protected int extractHash(final Key1 key1, final Key2 key2, final Key3 key3, final Key4 key4)
	{
		return (key1 != null ? key1.hashCode() : 3) ^ (key2 != null ? key2.hashCode() : 5) ^ (key3 != null ? key3.hashCode() : 7)
				^ (key4 != null ? key4.hashCode() : 11);
	}

	protected static int hash(int hash)
	{
		hash += ~(hash << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;
		return hash;
	}

	protected void addEntry(final int hash, Key1 key1, Key2 key2, Key3 key3, Key4 key4, final V value, final int bucketIndex)
	{
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> e = table[bucketIndex];
		e = createEntry(hash, key1, key2, key3, key4, value, e);
		table[bucketIndex] = e;
		entryAdded(e);
		if (size() >= threshold)
		{
			resize(2 * table.length);
		}
	}

	protected void entryAdded(final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry)
	{
		// Intended blank
	}

	protected void entryRemoved(final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry)
	{
		// Intended blank
	}

	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException
	{
		final int size = in.readInt();
		for (int a = 0; a < size; a++)
		{
			final Object key1 = in.readObject();
			final Object key2 = in.readObject();
			final Object key3 = in.readObject();
			final Object key4 = in.readObject();
			final Object value = in.readObject();
			put((Key1) key1, (Key2) key2, (Key3) key3, (Key4) key4, (V) value);
		}
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException
	{
		out.writeInt(size());

		final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;

		for (int a = table.length; a-- > 0;)
		{
			Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[a];
			while (entry != null)
			{
				out.writeObject(entry.getKey1());
				out.writeObject(entry.getKey2());
				out.writeObject(entry.getKey3());
				out.writeObject(entry.getKey4());
				out.writeObject(entry.getValue());
				entry = entry.getNextEntry();
			}
		}
	}

	/**
	 * Rehashes the contents of this map into a new array with a larger capacity. This method is called automatically when the number of keys in this map
	 * reaches its threshold. If current capacity is MAXIMUM_CAPACITY, this method does not resize the map, but sets threshold to Integer.MAX_VALUE. This has
	 * the effect of preventing future calls.
	 * 
	 * @param newCapacity
	 *            the new capacity, MUST be a power of two; must be greater than current capacity unless current capacity is MAXIMUM_CAPACITY (in which case
	 *            value is irrelevant).
	 */
	protected void resize(final int newCapacity)
	{
		final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] oldTable = table;
		final int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY)
		{
			threshold = Integer.MAX_VALUE;
			return;
		}

		final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] newTable = createTable(newCapacity);
		transfer(newTable);
		table = newTable;
		threshold = (int) (newCapacity * loadFactor);
	}

	protected void transfer(final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] newTable)
	{
		final int newCapacityMinus1 = newTable.length - 1;
		final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;

		for (int a = table.length; a-- > 0;)
		{
			Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[a], next;
			while (entry != null)
			{
				next = entry.getNextEntry();
				int i = entry.getHash() & newCapacityMinus1;
				entry.setNextEntry(newTable[i]);
				newTable[i] = entry;
				entry = next;
			}
		}
	}

	public V[] toArray(Class<V> arrayType)
	{
		@SuppressWarnings("unchecked")
		V[] array = (V[]) Array.newInstance(arrayType, size());
		return toArray(array);
	}

	public V[] toArray(final V[] targetArray)
	{
		int index = 0;
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;
		for (int a = table.length; a-- > 0;)
		{
			Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[a];
			while (entry != null)
			{
				targetArray[index++] = entry.getValue();
				entry = entry.getNextEntry();
			}
		}
		return targetArray;
	}

	public void clear()
	{
		if (isEmpty())
		{
			return;
		}
		final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;

		for (int a = table.length; a-- > 0;)
		{
			Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[a];
			if (entry != null)
			{
				table[a] = null;
				while (entry != null)
				{
					final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> nextEntry = entry.getNextEntry();
					entryRemoved(entry);
					entry = nextEntry;
				}
			}
		}
	}

	public boolean containsKey(final Key1 key1, final Key2 key2, final Key3 key3, final Key4 key4)
	{
		final int hash = hash(extractHash(key1, key2, key3, key4));
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;
		final int i = hash & (table.length - 1);
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[i];

		while (entry != null)
		{
			if (equalKeys(key1, key2, key3, key4, entry))
			{
				return true;
			}
			entry = entry.getNextEntry();
		}
		return false;
	}

	/**
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(final V value)
	{
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;
		if (value == null)
		{
			for (int a = table.length; a-- > 0;)
			{
				Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[a];
				while (entry != null)
				{
					final Object entryValue = entry.getValue();
					if (entryValue == null)
					{
						return true;
					}
					entry = entry.getNextEntry();
				}
			}
		}
		else
		{
			for (int a = table.length; a-- > 0;)
			{
				Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[a];
				while (entry != null)
				{
					final Object entryValue = entry.getValue();
					if (value.equals(entryValue))
					{
						return true;
					}
					entry = entry.getNextEntry();
				}
			}
		}
		return false;
	}

	protected boolean equalKeys(final Key1 key1, final Key2 key2, final Key3 key3, final Key4 key4, final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry)
	{
		return EqualsUtil.equals(key1, entry.getKey1()) && EqualsUtil.equals(key2, entry.getKey2()) && EqualsUtil.equals(key3, entry.getKey3())
				&& EqualsUtil.equals(key4, entry.getKey4());
	}

	public void putAll(final AbstractTuple4KeyHashMap<? extends Key1, ? extends Key2, ? extends Key3, ? extends Key4, ? extends V> map)
	{
		for (Tuple4KeyEntry<? extends Key1, ? extends Key2, ? extends Key3, ? extends Key4, ? extends V> entry : map)
		{
			put(entry.getKey1(), entry.getKey2(), entry.getKey3(), entry.getKey4(), entry.getValue());
		}
	}

	public V put(final Key1 key1, final Key2 key2, final Key3 key3, final Key4 key4, final V value)
	{
		final int hash = hash(extractHash(key1, key2, key3, key4));
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;
		final int i = hash & (table.length - 1);

		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[i];
		while (entry != null)
		{
			if (equalKeys(key1, key2, key3, key4, entry))
			{
				if (isSetValueForEntryAllowed())
				{
					return setValueForEntry(entry, value);
				}
				V oldValue = entry.getValue();
				removeEntryForKey(key1, key2, key3, key4);
				addEntry(hash, key1, key2, key3, key4, value, i);
				return oldValue;
			}
			entry = entry.getNextEntry();
		}
		addEntry(hash, key1, key2, key3, key4, value, i);
		return null;
	}

	public boolean putIfNotExists(final Key1 key1, final Key2 key2, final Key3 key3, final Key4 key4, final V value)
	{
		final int hash = hash(extractHash(key1, key2, key3, key4));
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;
		final int i = hash & (table.length - 1);

		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[i];
		while (entry != null)
		{
			if (equalKeys(key1, key2, key3, key4, entry))
			{
				return false;
			}
			entry = entry.getNextEntry();
		}
		addEntry(hash, key1, key2, key3, key4, value, i);
		return true;
	}

	public boolean removeIfValue(final Key1 key1, final Key2 key2, final Key3 key3, final Key4 key4, final V value)
	{
		final int hash = hash(extractHash(key1, key2, key3, key4));
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;
		final int i = hash & (table.length - 1);
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[i];
		if (entry != null)
		{
			if (equalKeys(key1, key2, key3, key4, entry))
			{
				table[i] = entry.getNextEntry();
				final V existingValue = entry.getValue();
				if (existingValue != value) // Test if reference identical
				{
					return false;
				}
				entryRemoved(entry);
				return true;
			}
			Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> prevEntry = entry;
			entry = entry.getNextEntry();
			while (entry != null)
			{
				if (equalKeys(key1, key2, key3, key4, entry))
				{
					prevEntry.setNextEntry(entry.getNextEntry());
					final V existingValue = entry.getValue();
					if (existingValue != value) // Test if reference identical
					{
						return false;
					}
					entryRemoved(entry);
					return true;
				}
				prevEntry = entry;
				entry = entry.getNextEntry();
			}
		}
		return false;
	}

	public V remove(final Key1 key1, final Key2 key2, final Key3 key3, final Key4 key4)
	{
		return removeEntryForKey(key1, key2, key3, key4);
	}

	protected final V removeEntryForKey(final Key1 key1, final Key2 key2, final Key3 key3, final Key4 key4)
	{
		final int hash = hash(extractHash(key1, key2, key3, key4));
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;
		final int i = hash & (table.length - 1);
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[i];
		if (entry != null)
		{
			if (equalKeys(key1, key2, key3, key4, entry))
			{
				table[i] = entry.getNextEntry();
				final V value = entry.getValue();
				entryRemoved(entry);
				return value;
			}
			Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> prevEntry = entry;
			entry = entry.getNextEntry();
			while (entry != null)
			{
				if (equalKeys(key1, key2, key3, key4, entry))
				{
					prevEntry.setNextEntry(entry.getNextEntry());
					final V value = entry.getValue();
					entryRemoved(entry);
					return value;
				}
				prevEntry = entry;
				entry = entry.getNextEntry();
			}
		}
		return null;
	}

	public V get(final Key1 key1, final Key2 key2, final Key3 key3, final Key4 key4)
	{
		final int hash = hash(extractHash(key1, key2, key3, key4));
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;
		final int i = hash & (table.length - 1);
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[i];
		while (entry != null)
		{
			if (equalKeys(key1, key2, key3, key4, entry))
			{
				return entry.getValue();
			}
			entry = entry.getNextEntry();
		}
		return null;
	}

	protected boolean isSetValueForEntryAllowed()
	{
		return true;
	}

	protected V setValueForEntry(final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry, final V value)
	{
		V oldValue = entry.getValue();
		entry.setValue(value);
		return oldValue;
	}

	protected abstract Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> createEntry(final int hash, final Key1 key1, final Key2 key2, final Key3 key3,
			final Key4 key4, final V value, final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> nextEntry);

	public abstract int size();

	public boolean isEmpty()
	{
		return size() == 0;
	}

	@Override
	public Iterator<Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>> iterator()
	{
		return new Tuple4KeyIterator<Key1, Key2, Key3, Key4, V>(this, table, true);
	}

	public IList<V> values()
	{
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;
		final ArrayList<V> valueList = new ArrayList<V>(size());
		for (int a = table.length; a-- > 0;)
		{
			Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[a];
			while (entry != null)
			{
				valueList.add(entry.getValue());
				entry = entry.getNextEntry();
			}
		}
		return valueList;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(size()).append(" items: [");
		boolean first = true;

		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;
		for (int a = table.length; a-- > 0;)
		{
			Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[a];
			while (entry != null)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(',');
				}
				StringBuilderUtil.appendPrintable(sb, entry);
				entry = entry.getNextEntry();
			}
		}
		sb.append(']');
	}
}
