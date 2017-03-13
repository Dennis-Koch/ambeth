package com.koch.ambeth.util.collections;

import java.util.Map;

public class WeakValueHashMap<K, V> extends WeakHashMap<K, V>
{
	public static <K, V> WeakValueHashMap<K, V> create(int size)
	{
		return create(size, DEFAULT_LOAD_FACTOR);
	}

	public static <K, V> WeakValueHashMap<K, V> create(int size, float loadFactor)
	{
		return new WeakValueHashMap<K, V>((int) (size / loadFactor) + 1, loadFactor);
	}

	public WeakValueHashMap()
	{
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, WeakValueMapEntry.class);
	}

	public WeakValueHashMap(float loadFactor)
	{
		this(DEFAULT_INITIAL_CAPACITY, loadFactor, WeakValueMapEntry.class);
	}

	public WeakValueHashMap(int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR, WeakValueMapEntry.class);
	}

	public WeakValueHashMap(int initialCapacity, float loadFactor)
	{
		this(initialCapacity, loadFactor, WeakValueMapEntry.class);
	}

	public WeakValueHashMap(int initialCapacity, float loadFactor, Class<?> entryClass)
	{
		super(initialCapacity, loadFactor, entryClass);
	}

	public WeakValueHashMap(Map<? extends K, ? extends V> map)
	{
		this((int) (map.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR, WeakValueMapEntry.class);
		putAll(map);
	}

	@Override
	protected IMapEntry<K, V> createEntry(int hash, K key, V value, final IMapEntry<K, V> nextEntry)
	{
		return new WeakValueMapEntry<K, V>(hash, nextEntry, key, value);
	}

	@Override
	protected void setNextEntry(final IMapEntry<K, V> entry, final IMapEntry<K, V> nextEntry)
	{
		((WeakValueMapEntry<K, V>) entry).setNextEntry(nextEntry);
	}

	@Override
	protected boolean isSetValueForEntryAllowed()
	{
		return false;
	}

	@Override
	protected V setValueForEntry(com.koch.ambeth.util.collections.IMapEntry<K, V> entry, V value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void transfer(IMapEntry<K, V>[] newTable)
	{
		final int newCapacityMinus1 = newTable.length - 1;
		final IMapEntry<K, V>[] table = this.table;

		for (int a = table.length; a-- > 0;)
		{
			IMapEntry<K, V> entry = table[a], next;
			entry = getEntryWithValidValue(entry);
			while (entry != null)
			{
				next = getEntryWithValidValue(entry.getNextEntry());
				int i = entry.getHash() & newCapacityMinus1;
				setNextEntry(entry, newTable[i]);
				newTable[i] = entry;
				entry = next;
			}
		}
	}

	protected IMapEntry<K, V> getEntryWithValidValue(IMapEntry<K, V> entry)
	{
		while (entry != null && entry.getValue() == null)
		{
			entry = entry.getNextEntry();
		}
		return entry;
	}
}
