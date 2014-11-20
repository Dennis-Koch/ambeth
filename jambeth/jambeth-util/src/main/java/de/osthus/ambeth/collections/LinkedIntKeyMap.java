package de.osthus.ambeth.collections;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Random;

public class LinkedIntKeyMap<V> implements Externalizable
{
	static final int DEFAULT_INITIAL_CAPACITY = 16;

	public static final int MAXIMUM_CAPACITY = 1 << 30;

	static final float DEFAULT_LOAD_FACTOR = 0.75f;

	private static final Random random = new Random();

	private LinkedIntKeyMapEntry<V>[] table;

	private final float loadFactor;

	private int threshold, tableLengthMinusOne;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private final GenericFastList<LinkedIntKeyMapEntry<V>> fastIterationList = new GenericFastList(LinkedIntKeyMapEntry.class);

	/**
	 * Constructs an empty <tt>HashMap</tt> with the default initial capacity (16) and the default load factor (0.75).
	 */
	public LinkedIntKeyMap()
	{
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Constructs an empty <tt>HashMap</tt> with the specified initial capacity and the default load factor (0.75).
	 * 
	 * @param initialCapacity
	 *            the initial capacity.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is negative.
	 */
	public LinkedIntKeyMap(int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	public LinkedIntKeyMap(float loadFactor)
	{
		this(DEFAULT_INITIAL_CAPACITY, loadFactor);
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
	public LinkedIntKeyMap(int initialCapacity, float loadFactor)
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
		table = new LinkedIntKeyMapEntry[capacity];
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

	protected LinkedIntKeyMapEntry<V> createEntry(final int hash, final int key, final V value, final LinkedIntKeyMapEntry<V> next)
	{
		LinkedIntKeyMapEntry<V> e = new LinkedIntKeyMapEntry<V>();
		e.initEntry(hash, key, value, next);
		return e;
	}

	protected void disposeEntry(final LinkedIntKeyMapEntry<V> entry)
	{
		// Intended blank
	}

	// /**
	// * Returns a hash value for the specified object. In addition to
	// * the object's own hashCode, this method applies a "supplemental
	// * hash function," which defends against poor quality hash functions.
	// * This is critical because HashMap uses power-of two length
	// * hash tables.<p>
	// *
	// * The shift distances in this function were chosen as the result
	// * of an automated search over the entire four-dimensional search space.
	// */
	// private static final int hash(int key)
	// {
	// key += ~(key << 9);
	// key ^= (key >>> 14);
	// key += (key << 4);
	// key ^= (key >>> 10);
	// return key;
	// }

	// protected void itemRemovedFromClear(final V item)
	// {
	// // if (disposeOnElementRemove)
	// // ((SerializeObject)item).dispose();
	// }

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
		return fastIterationList.size();
	}

	/**
	 * Returns <tt>true</tt> if this map contains no key-value mappings.
	 * 
	 * @return <tt>true</tt> if this map contains no key-value mappings.
	 */
	public final boolean isEmpty()
	{
		return size() == 0;
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

		LinkedIntKeyMapEntry<V> e = table[hash & tableLengthMinusOne];
		while (e != null)
		{
			if (key == e.key)
			{
				return e.value;
			}
			e = e.nextEntry;
		}
		return null;
	}

	public final boolean containsKey(final int key)
	{
		int hash = key + ~(key << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;

		LinkedIntKeyMapEntry<V> e = table[hash & tableLengthMinusOne];
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
	public final V put(final int key, final V value)
	{
		// if (value == null)
		// throw new IllegalArgumentException("Value must not be null");
		int hash = key + ~(key << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;

		final int i = hash & tableLengthMinusOne;

		LinkedIntKeyMapEntry<V> e = table[i];
		while (e != null)
		{
			if (key == e.key)
			{
				final V oldValue = e.value;
				e.value = value;
				return oldValue;
			}
			e = e.nextEntry;
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

		LinkedIntKeyMapEntry<V> e = table[i];
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
		LinkedIntKeyMapEntry<V> e;
		for (int i = table.length; i-- > 0;)
		{
			e = table[i];
			while (e != null)
			{
				if (value.equals(e.value))
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
	private final void addEntry(final int hash, final int key, final V value, final int bucketIndex)
	{
		LinkedIntKeyMapEntry<V> e = table[bucketIndex];
		e = createEntry(hash, key, value, e);
		table[bucketIndex] = e;
		fastIterationList.pushLast(e);
		if (size() >= threshold)
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
		LinkedIntKeyMapEntry<V> e = removeEntryForKey(key);
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
		int hash = key + ~(key << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;

		final int i = hash & tableLengthMinusOne;
		LinkedIntKeyMapEntry<V> e = table[i], prevEntry = null;

		while (e != null)
		{
			if (key == e.key)
			{
				if (e.value != object)
				{
					return false;
				}
				fastIterationList.remove(e);
				if (prevEntry == null)
				{
					table[i] = e.nextEntry;
				}
				else
				{
					prevEntry.nextEntry = e.nextEntry;
				}
				disposeEntry(e);
				return true;
			}
			prevEntry = e;
			e = e.nextEntry;
		}
		return false;
	}

	/**
	 * Removes and returns the entry associated with the specified key in the HashMap. Returns null if the HashMap contains no mapping for this key.
	 */
	private final LinkedIntKeyMapEntry<V> removeEntryForKey(final int key)
	{
		int hash = key + ~(key << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;

		final int i = hash & tableLengthMinusOne;
		LinkedIntKeyMapEntry<V> e = table[i], prevEntry = null;

		while (e != null)
		{
			if (key == e.key)
			{
				fastIterationList.remove(e);
				if (prevEntry == null)
				{
					table[i] = e.nextEntry;
				}
				else
				{
					prevEntry.nextEntry = e.nextEntry;
				}
				return e;
			}
			prevEntry = e;
			e = e.nextEntry;
		}
		return null;
	}

	/**
	 * Removes all mappings from this map.
	 */
	public final void clear()
	{
		if (isEmpty())
		{
			return;
		}
		int capacity = table.length;
		LinkedIntKeyMapEntry<V> pointer = fastIterationList.getFirstElem(), next;
		while (pointer != null)
		{
			next = pointer.next;
			int i = indexFor(pointer.hash, capacity);
			table[i] = null;
			disposeEntry(pointer);
			pointer = next;
		}
		fastIterationList.clear();
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
	@SuppressWarnings("unchecked")
	private final void resize(final int newCapacity)
	{
		final LinkedIntKeyMapEntry<V>[] oldTable = table;
		final int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY)
		{
			threshold = Integer.MAX_VALUE;
			return;
		}

		final LinkedIntKeyMapEntry<V>[] newTable = new LinkedIntKeyMapEntry[newCapacity];
		transfer(newTable);
		table = newTable;
		tableLengthMinusOne = table.length - 1;
		threshold = (int) (newCapacity * loadFactor);
	}

	/**
	 * Transfer all entries from current table to newTable.
	 */
	private final void transfer(LinkedIntKeyMapEntry<V>[] newTable)
	{
		final int newCapacity = newTable.length;
		LinkedIntKeyMapEntry<V> pointer = fastIterationList.getFirstElem(), next;
		while (pointer != null)
		{
			next = pointer.next;
			int i = indexFor(pointer.hash, newCapacity);
			pointer.nextEntry = newTable[i];
			newTable[i] = pointer;
			pointer = next;
		}
	}

	public final IntKeyIterator<V> iterator()
	{
		IntKeyIterator<V> iter = new IntKeyIterator<V>();
		iter.init(fastIterationList);
		return iter;
	}

	public final IntKeyIterator<V> iterator(IntKeyIterator<V> iter)
	{
		iter.init(fastIterationList);
		return iter;
	}

	public List<V> values(final List<V> list)
	{
		LinkedIntKeyMapEntry<V> pointer = fastIterationList.getFirstElem();
		while (pointer != null)
		{
			list.add(pointer.value);
			pointer = pointer.next;
		}
		return list;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException
	{
		int size = in.readInt(), key;
		for (int a = 0; a < size; a++)
		{
			key = in.readInt();
			Object value = in.readObject();
			put(key, (V) value);
		}
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException
	{
		out.writeInt(size());
		IntKeyIterator<V> iter = iterator();
		while (iter.hasNext())
		{
			LinkedIntKeyMapEntry<V> entry = iter.nextEntry();
			out.writeInt(entry.key);
			out.writeObject(entry.value);
		}
	}
}
