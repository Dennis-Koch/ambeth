package de.osthus.ambeth.collections;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

public class WeakHashMap<K, V> extends AbstractHashMap<Reference<K>, K, V>
{
	public static <K, V> WeakHashMap<K, V> create(int size)
	{
		return create(size, DEFAULT_LOAD_FACTOR);
	}

	public static <K, V> WeakHashMap<K, V> create(int size, float loadFactor)
	{
		return new WeakHashMap<K, V>((int) (size / loadFactor) + 1, loadFactor);
	}

	protected int size;

	protected final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();

	public WeakHashMap()
	{
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, WeakMapEntry.class);
	}

	public WeakHashMap(float loadFactor)
	{
		this(DEFAULT_INITIAL_CAPACITY, loadFactor, WeakMapEntry.class);
	}

	public WeakHashMap(int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR, WeakMapEntry.class);
	}

	public WeakHashMap(int initialCapacity, float loadFactor)
	{
		this(initialCapacity, loadFactor, WeakMapEntry.class);
	}

	public WeakHashMap(int initialCapacity, float loadFactor, Class<?> entryClass)
	{
		super(initialCapacity, loadFactor, entryClass);
	}

	@SuppressWarnings("unchecked")
	public Reference<K> getWeakReferenceEntry(K key)
	{
		final int hash = hash(extractHash(key));
		IMapEntry<K, V>[] table = this.table;
		final int i = hash & (table.length - 1);
		IMapEntry<K, V> entry = table[i];
		while (entry != null)
		{
			if (equalKeys(key, entry))
			{
				return (Reference<K>) entry;
			}
			entry = entry.getNextEntry();
		}
		return null;
	}

	@Override
	protected IMapEntry<K, V> createEntry(int hash, K key, V value, IMapEntry<K, V> nextEntry)
	{
		return new WeakMapEntry<K, V>(key, value, hash, nextEntry, referenceQueue);
	}

	@Override
	protected void entryAdded(final IMapEntry<K, V> entry)
	{
		size++;
		checkForCleanup();
	}

	@Override
	protected final void entryRemoved(IMapEntry<K, V> entry)
	{
		entryRemovedNoCleanup(entry);
		checkForCleanup();
	}

	protected void entryRemovedNoCleanup(IMapEntry<K, V> entry)
	{
		size--;
	}

	@Override
	protected void setNextEntry(IMapEntry<K, V> entry, IMapEntry<K, V> nextEntry)
	{
		((WeakMapEntry<K, V>) entry).setNextEntry(nextEntry);
	}

	@Override
	public int size()
	{
		return size;
	}

	@SuppressWarnings("unchecked")
	public void checkForCleanup()
	{
		IMapEntry<K, V>[] table = this.table;
		int tableLengthMinusOne = table.length - 1;
		IMapEntry<K, V> removedEntry;
		ReferenceQueue<Object> referenceQueue = this.referenceQueue;
		while ((removedEntry = (WeakMapEntry<K, V>) referenceQueue.poll()) != null)
		{
			int i = removedEntry.getHash() & tableLengthMinusOne;
			IMapEntry<K, V> entry = table[i];
			if (entry == null)
			{
				// Nothing to do
				continue;
			}
			if (entry == removedEntry)
			{
				table[i] = entry.getNextEntry();
				entryRemovedNoCleanup(entry);
				continue;
			}
			IMapEntry<K, V> prevEntry = entry;
			entry = entry.getNextEntry();
			while (entry != null)
			{
				if (entry == removedEntry)
				{
					setNextEntry(prevEntry, entry.getNextEntry());
					entryRemovedNoCleanup(entry);
					break;
				}
				prevEntry = entry;
				entry = entry.getNextEntry();
			}
		}
	}
}
