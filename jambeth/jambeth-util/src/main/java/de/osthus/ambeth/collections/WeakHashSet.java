package de.osthus.ambeth.collections;

import java.lang.ref.ReferenceQueue;
import java.util.Collection;
import java.util.Iterator;

public class WeakHashSet<K> extends AbstractHashSet<K>
{
	public static <K> WeakHashSet<K> create(int size)
	{
		return create(size, DEFAULT_LOAD_FACTOR);
	}

	public static <K> WeakHashSet<K> create(int size, float loadFactor)
	{
		return new WeakHashSet<K>((int) (size / loadFactor) + 1, loadFactor);
	}

	protected int size;

	protected final ReferenceQueue<K> referenceQueue = new ReferenceQueue<K>();

	public WeakHashSet()
	{
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, WeakSetEntry.class);
	}

	public WeakHashSet(Collection<? extends K> sourceCollection)
	{
		this((int) (sourceCollection.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR, WeakSetEntry.class);
		addAll(sourceCollection);
	}

	public WeakHashSet(K[] sourceArray)
	{
		this((int) (sourceArray.length / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR, WeakSetEntry.class);
		addAll(sourceArray);
	}

	public WeakHashSet(float loadFactor)
	{
		this(DEFAULT_INITIAL_CAPACITY, loadFactor, WeakSetEntry.class);
	}

	public WeakHashSet(int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR, WeakSetEntry.class);
	}

	public WeakHashSet(int initialCapacity, float loadFactor)
	{
		this(initialCapacity, loadFactor, SetEntry.class);
	}

	@SuppressWarnings("rawtypes")
	public WeakHashSet(int initialCapacity, float loadFactor, Class<? extends ISetEntry> entryClass)
	{
		super(initialCapacity, loadFactor, entryClass);
	}

	@Override
	protected ISetEntry<K> createEntry(int hash, K key, ISetEntry<K> nextEntry)
	{
		return new WeakSetEntry<K>(key, hash, (WeakSetEntry<K>) nextEntry, referenceQueue);
	}

	@Override
	protected void entryAdded(ISetEntry<K> entry)
	{
		size++;
		checkForCleanup();
	}

	@Override
	protected void entryRemoved(ISetEntry<K> entry)
	{
		size--;
		checkForCleanup();
	}

	@Override
	protected void setNextEntry(ISetEntry<K> entry, ISetEntry<K> nextEntry)
	{
		((WeakSetEntry<K>) entry).setNextEntry((WeakSetEntry<K>) nextEntry);
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public IList<K> toList()
	{
		IList<K> list = new ArrayList<K>(size());
		toList(list);
		return list;
	}

	@Override
	public void toList(Collection<K> targetList)
	{
		Iterator<K> iter = iterator();
		while (iter.hasNext())
		{
			K key = iter.next();
			if (key == null)
			{
				continue;
			}
			targetList.add(key);
		}
	}

	@SuppressWarnings("unchecked")
	public void checkForCleanup()
	{
		ArrayList<WeakSetEntry<K>> removes = null;
		WeakSetEntry<K> removedEntry;
		ReferenceQueue<K> referenceQueue = this.referenceQueue;
		while ((removedEntry = (WeakSetEntry<K>) referenceQueue.poll()) != null)
		{
			if (removes == null)
			{
				removes = new ArrayList<WeakSetEntry<K>>();
			}
			removes.add(removedEntry);
		}
		if (removes == null)
		{
			return;
		}
		for (int a = removes.size(); a-- > 0;)
		{
			removedEntry = removes.get(a);
			ISetEntry<K>[] table = this.table;
			int tableLengthMinusOne = table.length - 1;
			int i = removedEntry.hash & tableLengthMinusOne;
			ISetEntry<K> entry = table[i];
			if (entry == removedEntry)
			{
				table[i] = entry.getNextEntry();
				entryRemoved(entry);
				continue;
			}
			ISetEntry<K> prevEntry = entry;
			entry = entry.getNextEntry();
			while (entry != null)
			{
				if (entry == removedEntry)
				{
					setNextEntry(prevEntry, entry.getNextEntry());
					entryRemoved(entry);
					break;
				}
				prevEntry = entry;
				entry = entry.getNextEntry();
			}
		}
	}
}