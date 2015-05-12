package de.osthus.ambeth.cache.collections;

import de.osthus.ambeth.util.StringBuilderUtil;

/**
 * 66 percent faster compared to a normal HashMap with a Tuple2 (Composite-)Key as the Map-Key. This is due to the fact that there is no need to instantiate
 * Tuple2 Keys for put() or get() operations. Of course the overall memory footprint is also the half compared to a normal map: There is only the entry object
 * with 2 key-fields compared to the entry object compared to 1 key-field which contains a Tuple2 Key instance
 * 
 * @param <Key1>
 * @param <Key2>
 * @param
 */
public class CacheHashMap implements Iterable<CacheMapEntry>
{
	public static final int DEFAULT_INITIAL_CAPACITY = 16;

	public static final int MAXIMUM_CAPACITY = 1 << 30;

	public static final float DEFAULT_LOAD_FACTOR = 0.75f;

	protected final float loadFactor;

	protected int threshold;

	protected CacheMapEntry[] table;

	protected int size;

	protected final ICacheMapEntryTypeProvider cacheMapEntryTypeProvider;

	public CacheHashMap(ICacheMapEntryTypeProvider cacheMapEntryTypeProvider)
	{
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, cacheMapEntryTypeProvider);
	}

	public CacheHashMap(int initialCapacity, float loadFactor, ICacheMapEntryTypeProvider cacheMapEntryTypeProvider)
	{
		this.loadFactor = loadFactor;
		this.cacheMapEntryTypeProvider = cacheMapEntryTypeProvider;

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
	}

	protected CacheMapEntry[] createTable(final int capacity)
	{
		return new CacheMapEntry[capacity];
	}

	protected int extractHash(final Class<?> entityType, final Object id, final byte idIndex)
	{
		return entityType.hashCode() ^ id.hashCode() ^ idIndex;
	}

	protected static int hash(int hash)
	{
		hash += ~(hash << 9);
		hash ^= hash >>> 14;
		hash += hash << 4;
		hash ^= hash >>> 10;
		return hash;
	}

	protected void addEntry(final Class<?> entityType, final Object id, final byte idIndex, final Object value, final int bucketIndex)
	{
		CacheMapEntry[] table = this.table;
		CacheMapEntry e = table[bucketIndex];
		e = createEntry(entityType, id, idIndex, value, e);
		table[bucketIndex] = e;
		entryAdded(e);
		if (isResizeNeeded())
		{
			resize(2 * table.length);
		}
		if (value instanceof ICacheMapEntryAware)
		{
			((ICacheMapEntryAware) value).setCacheMapEntry(e);
		}
	}

	protected boolean isResizeNeeded()
	{
		return size() >= threshold;
	}

	protected void resize(final int newCapacity)
	{
		final CacheMapEntry[] oldTable = table;
		final int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY)
		{
			threshold = Integer.MAX_VALUE;
			return;
		}

		final CacheMapEntry[] newTable = createTable(newCapacity);
		transfer(newTable);
		table = newTable;
		threshold = (int) (newCapacity * loadFactor);
	}

	public boolean containsKey(final Class<?> entityType, final byte idIndex, final Object id)
	{
		final int hash = hash(extractHash(entityType, id, idIndex));
		CacheMapEntry[] table = this.table;
		final int i = hash & (table.length - 1);
		CacheMapEntry entry = table[i];

		while (entry != null)
		{
			if (equalKeys(entityType, id, idIndex, entry))
			{
				return true;
			}
			entry = entry.getNextEntry();
		}
		return false;
	}

	protected boolean equalKeys(final Class<?> entityType, final Object id, final byte idIndex, final CacheMapEntry entry)
	{
		try
		{
			return entry.isEqualTo(entityType, idIndex, id);
		}
		catch (RuntimeException e)
		{
			throw e;
		}
	}

	public Object put(final Class<?> entityType, final byte idIndex, final Object id, final Object value)
	{
		final int hash = hash(extractHash(entityType, id, idIndex));
		CacheMapEntry[] table = this.table;
		final int i = hash & (table.length - 1);

		CacheMapEntry entry = table[i];
		while (entry != null)
		{
			if (equalKeys(entityType, id, idIndex, entry))
			{
				return setValueForEntry(entry, value);
			}
			entry = entry.getNextEntry();
		}
		addEntry(entityType, id, idIndex, value, i);
		return null;
	}

	public boolean putIfNotExists(final Class<?> entityType, final byte idIndex, final Object id, final Object value)
	{
		final int hash = hash(extractHash(entityType, id, idIndex));
		CacheMapEntry[] table = this.table;
		final int i = hash & (table.length - 1);

		CacheMapEntry entry = table[i];
		while (entry != null)
		{
			if (equalKeys(entityType, id, idIndex, entry))
			{
				return false;
			}
			entry = entry.getNextEntry();
		}
		addEntry(entityType, id, idIndex, value, i);
		return true;
	}

	public boolean removeIfValue(final Class<?> entityType, final byte idIndex, final Object id, final Object value)
	{
		final int hash = hash(extractHash(entityType, id, idIndex));
		CacheMapEntry[] table = this.table;
		final int i = hash & (table.length - 1);
		CacheMapEntry entry = table[i];
		if (entry != null)
		{
			if (equalKeys(entityType, id, idIndex, entry))
			{
				table[i] = entry.getNextEntry();
				final Object existingValue = entry.getValue();
				if (existingValue != value) // Test if reference identical
				{
					return false;
				}
				entryRemoved(entry);
				return true;
			}
			CacheMapEntry prevEntry = entry;
			entry = entry.getNextEntry();
			while (entry != null)
			{
				if (equalKeys(entityType, id, idIndex, entry))
				{
					prevEntry.setNextEntry(entry.getNextEntry());
					final Object existingValue = entry.getValue();
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

	public Object remove(final Class<?> entityType, final byte idIndex, final Object id)
	{
		return removeEntryForKey(entityType, idIndex, id);
	}

	protected final Object removeEntryForKey(final Class<?> entityType, final byte idIndex, final Object id)
	{
		final int hash = hash(extractHash(entityType, id, idIndex));
		CacheMapEntry[] table = this.table;
		final int i = hash & (table.length - 1);
		CacheMapEntry entry = table[i];
		if (entry != null)
		{
			if (equalKeys(entityType, id, idIndex, entry))
			{
				table[i] = entry.getNextEntry();
				final Object value = entry.getValue();
				entryRemoved(entry);
				return value;
			}
			CacheMapEntry prevEntry = entry;
			entry = entry.getNextEntry();
			while (entry != null)
			{
				if (equalKeys(entityType, id, idIndex, entry))
				{
					prevEntry.setNextEntry(entry.getNextEntry());
					final Object value = entry.getValue();
					entryRemoved(entry);
					return value;
				}
				prevEntry = entry;
				entry = entry.getNextEntry();
			}
		}
		return null;
	}

	public Object get(final Class<?> entityType, final byte idIndex, final Object id)
	{
		final int hash = hash(extractHash(entityType, id, idIndex));
		CacheMapEntry[] table = this.table;
		final int i = hash & (table.length - 1);
		CacheMapEntry entry = table[i];
		while (entry != null)
		{
			if (equalKeys(entityType, id, idIndex, entry))
			{
				return entry.getValue();
			}
			entry = entry.getNextEntry();
		}
		return null;
	}

	protected Object setValueForEntry(final CacheMapEntry entry, final Object value)
	{
		Object oldValue = entry.getValue();
		entry.setValue(value);
		if (value instanceof ICacheMapEntryAware)
		{
			((ICacheMapEntryAware) value).setCacheMapEntry(entry);
		}
		return oldValue;
	}

	protected CacheMapEntry createEntry(final Class<?> entityType, final Object id, final byte idIndex, final Object value, final CacheMapEntry nextEntry)
	{
		ICacheMapEntryFactory factory = cacheMapEntryTypeProvider.getCacheMapEntryType(entityType, idIndex);
		return factory.createCacheMapEntry(entityType, idIndex, id, value, nextEntry);
	}

	public boolean isEmpty()
	{
		return size() == 0;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	public final int size()
	{
		return size;
	}

	protected void entryAdded(final CacheMapEntry e)
	{
		size++;
	}

	protected void entryRemoved(final CacheMapEntry e)
	{
		size--;
	}

	protected void transfer(final CacheMapEntry[] newTable)
	{
		final int newCapacityMinus1 = newTable.length - 1;
		final CacheMapEntry[] table = this.table;

		for (int a = table.length; a-- > 0;)
		{
			CacheMapEntry entry = table[a], next;
			while (entry != null)
			{
				next = entry.getNextEntry();
				int hash = hash(extractHash(entry.getEntityType(), entry.getId(), entry.getIdIndex()));
				int i = hash & newCapacityMinus1;
				entry.setNextEntry(newTable[i]);
				newTable[i] = entry;
				entry = next;
			}
		}
	}

	public Object[] toArray()
	{
		int index = 0;
		Object[] targetArray = new Object[size()];
		CacheMapEntry[] table = this.table;
		for (int a = table.length; a-- > 0;)
		{
			CacheMapEntry entry = table[a];
			while (entry != null)
			{
				targetArray[index++] = entry.getValue();
				entry = entry.getNextEntry();
			}
		}
		return targetArray;
	}

	@Override
	public CacheMapIterator iterator()
	{
		return new CacheMapIterator(this, table, true);
	}

	public CacheMapIterator iterator(boolean removeAllowed)
	{
		return new CacheMapIterator(this, table, removeAllowed);
	}

	public void clear()
	{
		if (isEmpty())
		{
			return;
		}
		final CacheMapEntry[] table = this.table;

		for (int a = table.length; a-- > 0;)
		{
			CacheMapEntry entry = table[a];
			if (entry != null)
			{
				table[a] = null;
				while (entry != null)
				{
					final CacheMapEntry nextEntry = entry.getNextEntry();
					entryRemoved(entry);
					entry = nextEntry;
				}
			}
		}
	}

	public void toString(StringBuilder sb)
	{
		sb.append(size()).append(" items: [");
		boolean first = true;

		CacheMapEntry[] table = this.table;
		for (int a = table.length; a-- > 0;)
		{
			CacheMapEntry entry = table[a];
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
