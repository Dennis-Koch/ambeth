package de.osthus.ambeth.collections;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class LinkedIntKeySet implements Externalizable
{
	public static class LinkedIntKeySetEntry
	{
		protected int key, hash;

		protected LinkedIntKeySetEntry next;

		protected final ListElem<LinkedIntKeySetEntry> listLE = new ListElem<LinkedIntKeySetEntry>(this);

		public final void init(final int key, final int hash, LinkedIntKeySetEntry next)
		{
			this.key = key;
			this.hash = hash;
			this.next = next;
		}
	}

	static final int DEFAULT_INITIAL_CAPACITY = 16;

	public static final int MAXIMUM_CAPACITY = 1 << 30;

	static final float DEFAULT_LOAD_FACTOR = 0.75f;

	private LinkedIntKeySetEntry[] table;

	private final float loadFactor;

	private int threshold, size, tableLengthMinusOne;

	private final FastList<LinkedIntKeySetEntry> fastIterationList = new FastList<LinkedIntKeySetEntry>();

	/**
	 * Constructs an empty <tt>HashMap</tt> with the default initial capacity (16) and the default load factor (0.75).
	 */
	public LinkedIntKeySet()
	{
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
		table = new LinkedIntKeySetEntry[DEFAULT_INITIAL_CAPACITY];
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
	public LinkedIntKeySet(int initialCapacity)
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
	public LinkedIntKeySet(int initialCapacity, float loadFactor)
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
		table = new LinkedIntKeySetEntry[capacity];
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

	public final boolean contains(final int key)
	{
		int hash = key + ~(key << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;

		LinkedIntKeySetEntry e = table[hash & tableLengthMinusOne];
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
		int hash = key + ~(key << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;

		final int i = hash & tableLengthMinusOne;

		LinkedIntKeySetEntry e = table[i];
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
		LinkedIntKeySetEntry oldEntry = table[bucketIndex];
		LinkedIntKeySetEntry e = new LinkedIntKeySetEntry();
		e.init(key, hash, oldEntry);
		table[bucketIndex] = e;
		fastIterationList.pushLast(e.listLE);
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
		LinkedIntKeySetEntry e = removeEntryForKey(key);
		if (e != null)
		{
			fastIterationList.remove(e.listLE);
			return true;
		}
		return false;
	}

	/**
	 * Removes and returns the entry associated with the specified key in the HashMap. Returns null if the HashMap contains no mapping for this key.
	 */
	private final LinkedIntKeySetEntry removeEntryForKey(final int key)
	{
		int hash = key + ~(key << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;

		final int i = hash & tableLengthMinusOne;
		LinkedIntKeySetEntry prev = table[i], e = prev, next;

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

	/**
	 * Removes all mappings from this map.
	 */
	public final void clear()
	{
		LinkedIntKeySetEntry e, next;

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
			fastIterationList.clear();
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
		final LinkedIntKeySetEntry[] oldTable = table;
		final int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY)
		{
			threshold = Integer.MAX_VALUE;
			return;
		}

		final LinkedIntKeySetEntry[] newTable = new LinkedIntKeySetEntry[newCapacity];
		transfer(newTable);
		table = newTable;
		tableLengthMinusOne = table.length - 1;
		threshold = (int) (newCapacity * loadFactor);
	}

	/**
	 * Transfer all entries from current table to newTable.
	 */
	private final void transfer(LinkedIntKeySetEntry[] newTable)
	{
		LinkedIntKeySetEntry[] src = table;
		final int newCapacity = newTable.length;
		int i;
		LinkedIntKeySetEntry e, next;
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

	public final LinkedIntSetIterator iterator()
	{
		LinkedIntSetIterator iter = new LinkedIntSetIterator();
		iter.init(fastIterationList);
		return iter;
	}

	public final void iterator(LinkedIntSetIterator iter)
	{
		iter.init(fastIterationList);
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException
	{
		int size = in.readInt();
		for (int a = 0; a < size; a++)
		{
			add(in.readInt());
		}
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException
	{
		out.writeInt(size());
		LinkedIntSetIterator iter = iterator();
		while (iter.hasNext())
		{
			out.writeInt(iter.next());
		}
	}
}
