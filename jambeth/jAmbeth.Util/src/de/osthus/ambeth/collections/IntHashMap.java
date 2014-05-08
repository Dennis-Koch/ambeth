package de.osthus.ambeth.collections;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class IntHashMap implements Externalizable
{
	public static class IntIterator
	{
		private IListElem<IntHashMapEntry> currPointer;

		public void init(final InterfaceFastList<IntHashMapEntry> listToIterate)
		{
			this.currPointer = listToIterate.first();
		}

		public final boolean hasNext()
		{
			return currPointer != null;
		}

		public final int next()
		{
			try
			{
				return currPointer.getElemValue().getValue();
			}
			finally
			{
				currPointer = currPointer.getNext();
			}
		}

		public final IntHashMapEntry nextEntry()
		{
			try
			{
				return currPointer.getElemValue();
			}
			finally
			{
				currPointer = currPointer.getNext();
			}
		}
	}

	public static class IntHashMapEntry implements IListElem<IntHashMapEntry>
	{
		private Object listHandle;

		private IListElem<IntHashMapEntry> prev, next;

		private final int key, hash;

		private int value;

		private IntHashMapEntry nextEntry;

		public IntHashMapEntry(final int hash, final int key, final int value, IntHashMapEntry nextEntry)
		{
			this.hash = hash;
			this.key = key;
			this.value = value;
			this.nextEntry = nextEntry;
		}

		public final int getValue()
		{
			return value;
		}

		public final int getKey()
		{
			return key;
		}

		@Override
		public IntHashMapEntry getElemValue()
		{
			return this;
		}

		@Override
		public void setElemValue(IntHashMapEntry elemValue)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object getListHandle()
		{
			return listHandle;
		}

		@Override
		public void setListHandle(Object listHandle)
		{
			this.listHandle = listHandle;
		}

		@Override
		public IListElem<IntHashMapEntry> getNext()
		{
			return next;
		}

		@Override
		public void setNext(IListElem<IntHashMapEntry> next)
		{
			this.next = next;
		}

		@Override
		public IListElem<IntHashMapEntry> getPrev()
		{
			return prev;
		}

		@Override
		public void setPrev(IListElem<IntHashMapEntry> prev)
		{
			this.prev = prev;
		}
	}

	static final int DEFAULT_INITIAL_CAPACITY = 16;

	public static final int MAXIMUM_CAPACITY = 1 << 30;

	static final float DEFAULT_LOAD_FACTOR = 0.75f;

	static final boolean DEFAULT_FAST_LIST = true;

	private IntHashMapEntry[] table;

	private final float loadFactor;

	private int threshold, size, tableLengthMinusOne;

	private boolean fastListIteration = false;

	private InterfaceFastList<IntHashMapEntry> fastIterationList;

	/**
	 * Constructs an empty <tt>HashMap</tt> with the default initial capacity (16) and the default load factor (0.75).
	 */
	public IntHashMap()
	{
		this(DEFAULT_FAST_LIST);
	}

	public IntHashMap(final boolean fastListIteration)
	{
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
		table = new IntHashMapEntry[DEFAULT_INITIAL_CAPACITY];
		tableLengthMinusOne = table.length - 1;
		init();
		setFastListIteration(fastListIteration);
	}

	/**
	 * Constructs an empty <tt>HashMap</tt> with the specified initial capacity and the default load factor (0.75).
	 * 
	 * @param initialCapacity
	 *            the initial capacity.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is negative.
	 */
	public IntHashMap(int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_FAST_LIST);
	}

	public IntHashMap(int initialCapacity, boolean fastListIteration)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR, fastListIteration);
	}

	public IntHashMap(int initialCapacity, float loadFactor)
	{
		this(initialCapacity, loadFactor, DEFAULT_FAST_LIST);
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
	public IntHashMap(int initialCapacity, float loadFactor, boolean fastListIteration)
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
		table = new IntHashMapEntry[capacity];
		tableLengthMinusOne = table.length - 1;
		init();
		setFastListIteration(fastListIteration);
	}

	public final void setFastListIteration(final boolean ifastListIteration)
	{
		if (fastListIteration != ifastListIteration)
		{
			fastListIteration = ifastListIteration;
			if (fastListIteration) // true
			{
				fastIterationList = new InterfaceFastList<IntHashMapEntry>();
				IntHashMapEntry e;
				for (int i = table.length; i-- > 0;)
				{
					e = table[i];
					while (e != null)
					{
						fastIterationList.pushLast(e);
						e = e.nextEntry;
					}
				}
			}
			else
			// false
			{
				fastIterationList = null;
			}
		}
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
	 * @see #put(int, int)
	 */
	public final int get(final int key)
	{
		int hash = key + (~(key << 9));
		hash ^= (hash >>> 14);
		hash += (hash << 4);
		hash ^= (hash >>> 10);

		IntHashMapEntry e = table[hash & tableLengthMinusOne];
		while (e != null)
		{
			if (key == e.key)
			{
				return e.value;
			}
			e = e.nextEntry;
		}
		return 0;
	}

	public final boolean containsKey(final int key)
	{
		int hash = key + (~(key << 9));
		hash ^= (hash >>> 14);
		hash += (hash << 4);
		hash ^= (hash >>> 10);

		IntHashMapEntry e = table[hash & tableLengthMinusOne];
		while (e != null)
		{
			if (key == e.key)
			{
				return true;
			}
			e = e.nextEntry;
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
	public final int put(final int key, final int value)
	{
		// if (value == null)
		// throw new IllegalArgumentException("Value must not be null");
		int hash = key + (~(key << 9));
		hash ^= (hash >>> 14);
		hash += (hash << 4);
		hash ^= (hash >>> 10);

		final int i = hash & tableLengthMinusOne;

		IntHashMapEntry e = table[i];
		while (e != null)
		{
			if (key == e.key)
			{
				final int oldValue = e.value;
				e.value = value;
				return oldValue;
			}
			e = e.nextEntry;
		}
		addEntry(hash, key, value, i);
		return 0;
	}

	public final boolean putIfNotExists(final int key, final int value)
	{
		int hash = key + (~(key << 9));
		hash ^= (hash >>> 14);
		hash += (hash << 4);
		hash ^= (hash >>> 10);

		final int i = hash & tableLengthMinusOne;

		IntHashMapEntry e = table[i];
		while (e != null)
		{
			if (key == e.key)
			{
				return false;
			}
			e = e.nextEntry;
		}
		addEntry(hash, key, value, i);
		return true;
	}

	public final int putIfNotExists(final int value)
	{
		int key;
		while (true)
		{
			key = (int) (Math.random() * Integer.MAX_VALUE);
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
	public final boolean containsValue(final int value)
	{
		IntHashMapEntry e;
		for (int i = table.length; i-- > 0;)
		{
			e = table[i];
			while (e != null)
			{
				if (value == e.value)
				{
					return true;
				}
				e = e.nextEntry;
			}
		}
		return false;
	}

	/**
	 * Add a new entry with the specified key, value and hash code to the specified bucket. It is the responsibility of this method to resize the table if
	 * appropriate.
	 * 
	 * Subclass overrides this to alter the behavior of put method.
	 */
	private final void addEntry(final int hash, final int key, final int value, final int bucketIndex)
	{
		IntHashMapEntry next = table[bucketIndex];
		IntHashMapEntry e = new IntHashMapEntry(hash, key, value, next);
		table[bucketIndex] = e;
		if (fastListIteration)
		{
			fastIterationList.pushLast(e);
		}
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
	public final int remove(final int key)
	{
		IntHashMapEntry e = removeEntryForKey(key);
		if (e != null)
		{
			final int value = e.value;
			if (fastListIteration)
			{
				fastIterationList.remove(e);
			}
			return value;
		}
		return 0;
	}

	public final boolean removeIfObject(final int key, final int object)
	{
		IntHashMapEntry e = removeEntryForKeyIfObject(key, object);
		if (e != null)
		{
			if (fastListIteration)
			{
				fastIterationList.remove(e);
			}
			return true;
		}
		return false;
	}

	/**
	 * Removes and returns the entry associated with the specified key in the HashMap. Returns null if the HashMap contains no mapping for this key.
	 */
	private final IntHashMapEntry removeEntryForKey(final int key)
	{
		int hash = key + (~(key << 9));
		hash ^= (hash >>> 14);
		hash += (hash << 4);
		hash ^= (hash >>> 10);

		final int i = hash & tableLengthMinusOne;
		IntHashMapEntry prev = table[i], e = prev, next;

		while (e != null)
		{
			next = e.nextEntry;
			if (key == e.key)
			{
				size--;
				if (prev == e)
				{
					table[i] = next;
				}
				else
				{
					prev.nextEntry = next;
				}
				return e;
			}
			prev = e;
			e = next;
		}
		return e;
	}

	private final IntHashMapEntry removeEntryForKeyIfObject(final int key, final int value)
	{
		int hash = key + (~(key << 9));
		hash ^= (hash >>> 14);
		hash += (hash << 4);
		hash ^= (hash >>> 10);

		final int i = hash & tableLengthMinusOne;
		IntHashMapEntry prev = table[i], e = prev, next;

		while (e != null)
		{
			next = e.nextEntry;
			if (key == e.key)
			{
				if (e.value != value)
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
					prev.nextEntry = next;
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
		if (size > 0)
		{
			if (fastListIteration)
			{
				IListElem<IntHashMapEntry> pointer = fastIterationList.first();
				while (pointer != null)
				{
					IntHashMapEntry e = pointer.getElemValue();
					table[e.hash & tableLengthMinusOne] = null;
					pointer = pointer.getNext();
				}
				fastIterationList.clear();
			}
			else
			{
				for (int i = table.length; i-- > 0;)
				{
					table[i] = null;
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
		final IntHashMapEntry[] oldTable = table;
		final int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY)
		{
			threshold = Integer.MAX_VALUE;
			return;
		}

		final IntHashMapEntry[] newTable = new IntHashMapEntry[newCapacity];
		transfer(newTable);
		table = newTable;
		tableLengthMinusOne = table.length - 1;
		threshold = (int) (newCapacity * loadFactor);
	}

	/**
	 * Transfer all entries from current table to newTable.
	 */
	private final void transfer(IntHashMapEntry[] newTable)
	{
		IntHashMapEntry[] src = table;
		final int newTableLengthMinusOne = newTable.length;
		int i;
		IntHashMapEntry e, next;
		for (int j = 0; j < src.length; j++)
		{
			e = src[j];
			if (e != null)
			{
				src[j] = null;
				do
				{
					next = e.nextEntry;
					i = e.hash & newTableLengthMinusOne;
					e.nextEntry = newTable[i];
					newTable[i] = e;
					e = next;
				}
				while (e != null);
			}
		}
	}

	public final IntIterator iterator()
	{
		if (!fastListIteration)
		{
			setFastListIteration(true);
		}
		IntIterator iter = new IntIterator();
		iter.init(fastIterationList);
		return iter;
	}

	public final void iterator(IntIterator iter)
	{
		if (!fastListIteration)
		{
			setFastListIteration(true);
		}
		iter.init(fastIterationList);
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException
	{
		int size = in.readInt(), key, value;
		for (int a = 0; a < size; a++)
		{
			key = in.readInt();
			value = in.readInt();
			put(key, value);
		}
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException
	{
		out.writeInt(size());
		IntIterator iter = iterator();
		IntHashMapEntry entry;
		while (iter.hasNext())
		{
			entry = iter.nextEntry();
			out.writeInt(entry.getKey());
			out.writeInt(entry.getValue());
		}
	}
}
