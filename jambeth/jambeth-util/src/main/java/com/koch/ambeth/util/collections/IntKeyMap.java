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

import java.util.List;
import java.util.Random;

public class IntKeyMap<V>
{
	public static class IntKeyMapEntry<V>
	{
		private int key, hash;

		private V value;

		private IntKeyMapEntry<V> next;

		/**
		 * Create new entry.
		 */
		public IntKeyMapEntry()
		{
		}

		public void initEntry(final int hash, final int key, final V value, IntKeyMapEntry<V> next)
		{
			this.value = value;
			this.next = next;
			this.key = key;
			this.hash = hash;
		}

		public final V getValue()
		{
			return value;
		}

		public final int getKey()
		{
			return key;
		}
	}

	static final int DEFAULT_INITIAL_CAPACITY = 16;

	public static final int MAXIMUM_CAPACITY = 1 << 30;

	static final float DEFAULT_LOAD_FACTOR = 0.75f;

	private static final Random random = new Random();

	private IntKeyMapEntry<V>[] table;

	private final float loadFactor;

	private int threshold, size, tableLengthMinusOne;

	/**
	 * Constructs an empty <tt>HashMap</tt> with the default initial capacity (16) and the default load factor (0.75).
	 */
	@SuppressWarnings("unchecked")
	public IntKeyMap()
	{
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);

		table = new IntKeyMapEntry[DEFAULT_INITIAL_CAPACITY];
		tableLengthMinusOne = table.length - 1;
		init();
	}

	/**
	 * Constructs an empty <tt>HashMap</tt> with the specified initial capacity and the default load factor (0.75).
	 * 
	 * @param initialCapacity
	 *            the initial capacity.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is negative.
	 */
	public IntKeyMap(int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Constructs an empty <tt>HashMap</tt> with the specified initial capacity and load factor.
	 * 
	 * @param initialCapacity
	 *            The initial capacity.
	 * @param loadFactor
	 *            The load factor.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is negative or the load factor is nonpositive.
	 */
	@SuppressWarnings("unchecked")
	public IntKeyMap(int initialCapacity, float loadFactor)
	{
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

		this.loadFactor = loadFactor;
		threshold = (int) (capacity * loadFactor);
		table = new IntKeyMapEntry[capacity];
		tableLengthMinusOne = table.length - 1;
		init();
	}

	/**
	 * Initialization hook for subclasses. This method is called in all constructors and pseudo-constructors (clone, readObject) after HashMap has been
	 * initialized but before any entries have been inserted. (In the absence of this method, readObject would require explicit knowledge of subclasses.)
	 */
	protected void init()
	{
		// Intended blank
	}

	protected IntKeyMapEntry<V> createEntry(final int hash, final int key, final V value, final IntKeyMapEntry<V> next)
	{
		IntKeyMapEntry<V> e = new IntKeyMapEntry<V>();
		e.initEntry(hash, key, value, next);
		return e;
	}

	protected void disposeEntry(final IntKeyMapEntry<V> entry)
	{
		// Intended blank
	}

	/**
	 * Returns index for hash code h.
	 */
	private static final int indexFor(final int h, final int length)
	{
		return h & length - 1;
	}

	/**
	 * Returns the number of key-value mappings in this map.
	 * 
	 * @return the number of key-value mappings in this map.
	 */
	public final int size()
	{
		return size;
	}

	/**
	 * Returns <tt>true</tt> if this map contains no key-value mappings.
	 * 
	 * @return <tt>true</tt> if this map contains no key-value mappings.
	 */
	public final boolean isEmpty()
	{
		return size == 0;
	}

	/**
	 * Returns the value to which the specified key is mapped in this identity hash map, or <tt>null</tt> if the map contains no mapping for this key. A return
	 * value of <tt>null</tt> does not <i>necessarily</i> indicate that the map contains no mapping for the key; it is also possible that the map explicitly
	 * maps the key to <tt>null</tt>. The <tt>containsKey</tt> method may be used to distinguish these two cases.
	 * 
	 * @param key
	 *            the key whose associated value is to be returned.
	 * @return the value to which this map maps the specified key, or <tt>null</tt> if the map contains no mapping for this key.
	 * @see #put(Object, Object)
	 */
	public final V get(final int key)
	{
		int hash = key + ~(key << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;

		IntKeyMapEntry<V> e = table[hash & tableLengthMinusOne];
		while (e != null)
		{
			if (key == e.key)
			{
				return e.value;
			}
			e = e.next;
		}
		return null;
	}

	public final boolean containsKey(final int key)
	{
		int hash = key + ~(key << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;

		IntKeyMapEntry<V> e = table[hash & tableLengthMinusOne];
		while (e != null)
		{
			if (key == e.key)
			{
				return true;
			}
			e = e.next;
		}
		return false;
	}

	/**
	 * Associates the specified value with the specified key in this map. If the map previously contained a mapping for this key, the old value is replaced.
	 * 
	 * @param key
	 *            key with which the specified value is to be associated.
	 * @param value
	 *            value to be associated with the specified key.
	 * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key. A <tt>null</tt> return can also indicate that the
	 *         HashMap previously associated <tt>null</tt> with the specified key.
	 */
	public final V put(final int key, final V value)
	{
		// if (value == null)
		// throw new IllegalArgumentException("Value must not be null");
		int hash = key + ~(key << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;

		final int i = hash & tableLengthMinusOne;

		IntKeyMapEntry<V> e = table[i];
		while (e != null)
		{
			if (key == e.key)
			{
				final V oldValue = e.value;
				e.value = value;
				return oldValue;
			}
			e = e.next;
		}
		addEntry(hash, key, value, i);
		return null;
	}

	public final boolean putIfNotExists(final int key, final V value)
	{
		int hash = key + ~(key << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;

		final int i = hash & tableLengthMinusOne;

		IntKeyMapEntry<V> e = table[i];
		while (e != null)
		{
			if (key == e.key)
			{
				return false;
			}
			e = e.next;
		}
		addEntry(hash, key, value, i);
		return true;
	}

	public final int putIfNotExists(final V value)
	{
		int key;
		while (true)
		{
			key = random.nextInt();
			if (putIfNotExists(key, value))
			{
				return key;
			}
		}
	}

	/**
	 * Returns <tt>true</tt> if this map maps one or more keys to the specified value.
	 * 
	 * @param value
	 *            value whose presence in this map is to be tested.
	 * @return <tt>true</tt> if this map maps one or more keys to the specified value.
	 */
	public final boolean containsValue(final V value)
	{
		if (value == null)
		{
			return false;
		}
		IntKeyMapEntry<V> e;
		for (int i = table.length; i-- > 0;)
		{
			e = table[i];
			while (e != null)
			{
				if (value.equals(e.value))
				{
					return true;
				}
				e = e.next;
			}
		}
		return false;
	}

	/**
	 * Add a new entry with the specified key, value and hash code to the specified bucket. It is the responsibility of this method to resize the table if
	 * appropriate. Subclass overrides this to alter the behavior of put method.
	 */
	private final void addEntry(final int hash, final int key, final V value, final int bucketIndex)
	{
		IntKeyMapEntry<V> e = table[bucketIndex];
		e = createEntry(hash, key, value, e);
		table[bucketIndex] = e;
		if (size++ >= threshold)
		{
			resize(2 * table.length);
		}
	}

	/**
	 * Removes the mapping for this key from this map if present.
	 * 
	 * @param key
	 *            key whose mapping is to be removed from the map.
	 * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key. A <tt>null</tt> return can also indicate that the
	 *         map previously associated <tt>null</tt> with the specified key.
	 */
	public final V remove(final int key)
	{
		IntKeyMapEntry<V> e = removeEntryForKey(key);
		if (e != null)
		{
			final V value = e.value;
			disposeEntry(e);
			return value;
		}
		return null;
	}

	public final boolean removeIfObject(final int key, final V object)
	{
		IntKeyMapEntry<V> e = removeEntryForKeyIfObject(key, object);
		if (e != null)
		{
			disposeEntry(e);
			return true;
		}
		return false;
	}

	public final void removeAndNotify(final int key)
	{
		IntKeyMapEntry<V> e = removeEntryForKey(key);
		if (e != null)
		{
			disposeEntry(e);
		}
	}

	/**
	 * Removes and returns the entry associated with the specified key in the HashMap. Returns null if the HashMap contains no mapping for this key.
	 */
	private final IntKeyMapEntry<V> removeEntryForKey(final int key)
	{
		int hash = key + ~(key << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;

		final int i = hash & tableLengthMinusOne;
		IntKeyMapEntry<V> prev = table[i], e = prev, next;

		while (e != null)
		{
			next = e.next;
			if (key == e.key)
			{
				size--;
				if (prev == e)
				{
					table[i] = next;
				}
				else
				{
					prev.next = next;
				}
				return e;
			}
			prev = e;
			e = next;
		}
		return e;
	}

	private final IntKeyMapEntry<V> removeEntryForKeyIfObject(final int key, final V object)
	{
		int hash = key + ~(key << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;

		final int i = hash & tableLengthMinusOne;
		IntKeyMapEntry<V> prev = table[i], e = prev, next;

		while (e != null)
		{
			next = e.next;
			if (key == e.key)
			{
				if (e.value != object)
				{
					return null;
				}
				size--;
				if (prev == e)
				{
					table[i] = next;
				}
				else
				{
					prev.next = next;
				}
				return e;
			}
			prev = e;
			e = next;
		}
		return null;
	}

	/**
	 * Removes all mappings from this map.
	 */
	public final void clear()
	{
		IntKeyMapEntry<V> e, next;

		if (size > 0)
		{
			for (int i = table.length; i-- > 0;)
			{
				e = table[i];
				if (e != null)
				{
					table[i] = null;
					while (e != null)
					{
						next = e.next;
						disposeEntry(e);
						e = next;
					}
				}
			}
			size = 0;
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
	@SuppressWarnings("unchecked")
	private final void resize(final int newCapacity)
	{
		final IntKeyMapEntry<V>[] oldTable = table;
		final int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY)
		{
			threshold = Integer.MAX_VALUE;
			return;
		}

		final IntKeyMapEntry<V>[] newTable = new IntKeyMapEntry[newCapacity];
		transfer(newTable);
		table = newTable;
		tableLengthMinusOne = table.length - 1;
		threshold = (int) (newCapacity * loadFactor);
	}

	/**
	 * Transfer all entries from current table to newTable.
	 */
	private final void transfer(IntKeyMapEntry<V>[] newTable)
	{
		IntKeyMapEntry<V>[] src = table;
		final int newCapacity = newTable.length;
		int i;
		IntKeyMapEntry<V> e, next;
		for (int j = 0; j < src.length; j++)
		{
			e = src[j];
			if (e != null)
			{
				src[j] = null;
				do
				{
					next = e.next;
					i = indexFor(e.hash, newCapacity);
					e.next = newTable[i];
					newTable[i] = e;
					e = next;
				}
				while (e != null);
			}
		}
	}

	public ISet<IntKeyMapEntry<V>> entrySet()
	{
		final LinkedHashSet<IntKeyMapEntry<V>> entrySet = new LinkedHashSet<IntKeyMapEntry<V>>((int) (size() / DEFAULT_LOAD_FACTOR) + 1);
		entrySet(entrySet);
		return entrySet;
	}

	public void entrySet(ISet<IntKeyMapEntry<V>> targetEntrySet)
	{
		for (int a = table.length; a-- > 0;)
		{
			IntKeyMapEntry<V> e = table[a];
			while (e != null)
			{
				targetEntrySet.add(e);
				e = e.next;
			}
		}
	}

	public List<V> values()
	{
		ArrayList<V> list = new ArrayList<V>(size());
		for (int i = table.length; i-- > 0;)
		{
			IntKeyMapEntry<V> e = table[i];
			while (e != null)
			{
				list.add(e.value);
				e = e.next;
			}
		}
		return list;
	}
}
