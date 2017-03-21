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

public class IntKeySet
{
	public static class IntKeySetEntry
	{
		private int key, hash;

		private IntKeySetEntry next;

		/**
		 * Create new entry.
		 */
		public IntKeySetEntry()
		{
		}

		protected final void init(final int ihash, final int ikey, IntKeySetEntry inext)
		{
			next = inext;
			key = ikey;
			hash = ihash;
		}

		public final int getKey()
		{
			return key;
		}
	}

	static final int DEFAULT_INITIAL_CAPACITY = 16;

	public static final int MAXIMUM_CAPACITY = 1 << 30;

	static final float DEFAULT_LOAD_FACTOR = 0.75f;

	private IntKeySetEntry[] table;

	private final float loadFactor;

	private int threshold, size, tableLengthMinusOne;

	/**
	 * Constructs an empty <tt>HashMap</tt> with the default initial capacity (16) and the default load factor (0.75).
	 */
	public IntKeySet()
	{
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
		table = new IntKeySetEntry[DEFAULT_INITIAL_CAPACITY];
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
	public IntKeySet(int initialCapacity)
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
	public IntKeySet(int initialCapacity, float loadFactor)
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
		table = new IntKeySetEntry[capacity];
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

	/**
	 * Returns index for hash code h.
	 */
	private static final int indexFor(final int h, final int length)
	{
		return h & (length - 1);
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

	public final boolean contains(final int key)
	{
		int hash = key + (~(key << 9));
		hash ^= (hash >>> 14);
		hash += (hash << 4);
		hash ^= (hash >>> 10);

		IntKeySetEntry e = table[hash & tableLengthMinusOne];
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
	public final boolean add(final int key)
	{
		int hash = key + (~(key << 9));
		hash ^= (hash >>> 14);
		hash += (hash << 4);
		hash ^= (hash >>> 10);

		final int i = hash & tableLengthMinusOne;

		IntKeySetEntry e = table[i];
		while (e != null)
		{
			if (key == e.key)
			{
				return false;
			}
			e = e.next;
		}
		addEntry(hash, key, i);
		return true;
	}

	/**
	 * Add a new entry with the specified key, value and hash code to the specified bucket. It is the responsibility of this method to resize the table if
	 * appropriate.
	 * 
	 * Subclass overrides this to alter the behavior of put method.
	 */
	private final void addEntry(final int hash, final int key, final int bucketIndex)
	{
		IntKeySetEntry e = table[bucketIndex];
		IntKeySetEntry newEntry = new IntKeySetEntry();
		newEntry.init(hash, key, e);
		table[bucketIndex] = newEntry;
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
	public final boolean remove(final int key)
	{
		IntKeySetEntry e = removeEntryForKey(key);
		if (e != null)
		{
			return true;
		}
		return false;
	}

	/**
	 * Removes and returns the entry associated with the specified key in the HashMap. Returns null if the HashMap contains no mapping for this key.
	 */
	private final IntKeySetEntry removeEntryForKey(final int key)
	{
		int hash = key + (~(key << 9));
		hash ^= (hash >>> 14);
		hash += (hash << 4);
		hash ^= (hash >>> 10);

		final int i = hash & tableLengthMinusOne;
		IntKeySetEntry entry = table[i];
		if (entry != null)
		{
			if (key == entry.getKey())
			{
				table[i] = entry.next;
				size--;
				return entry;
			}
			IntKeySetEntry prevEntry = entry;
			entry = entry.next;
			while (entry != null)
			{
				if (key == entry.getKey())
				{
					prevEntry.next = entry.next;
					return entry;
				}
				prevEntry = entry;
				entry = entry.next;
			}
		}
		return entry;
	}

	/**
	 * Removes all mappings from this map.
	 */
	public final void clear()
	{
		IntKeySetEntry e, next;

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
						e = next;
					}
				}
			}
			size = 0;
		}
	}

	/**
	 * Rehashes the contents of this map into a new array with a larger capacity. This method is called automatically when the number of keys in this map
	 * reaches its threshold.
	 * 
	 * If current capacity is MAXIMUM_CAPACITY, this method does not resize the map, but sets threshold to Integer.MAX_VALUE. This has the effect of preventing
	 * future calls.
	 * 
	 * @param newCapacity
	 *            the new capacity, MUST be a power of two; must be greater than current capacity unless current capacity is MAXIMUM_CAPACITY (in which case
	 *            value is irrelevant).
	 */
	private final void resize(final int newCapacity)
	{
		final IntKeySetEntry[] oldTable = table;
		final int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY)
		{
			threshold = Integer.MAX_VALUE;
			return;
		}

		final IntKeySetEntry[] newTable = new IntKeySetEntry[newCapacity];
		transfer(newTable);
		table = newTable;
		tableLengthMinusOne = table.length - 1;
		threshold = (int) (newCapacity * loadFactor);
	}

	/**
	 * Transfer all entries from current table to newTable.
	 */
	private final void transfer(IntKeySetEntry[] newTable)
	{
		IntKeySetEntry[] src = table;
		final int newCapacity = newTable.length;
		int i;
		IntKeySetEntry e, next;
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
}
