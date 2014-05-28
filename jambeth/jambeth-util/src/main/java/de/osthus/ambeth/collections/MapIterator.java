package de.osthus.ambeth.collections;

import java.util.Map.Entry;

public class MapIterator<WrappedK, K, V> extends AbstractIterator<Entry<K, V>>
{
	protected IMapEntry<K, V> currEntry, nextEntry;

	protected int index;

	protected final IMapEntry<K, V>[] table;

	private final AbstractHashMap<WrappedK, K, V> hashMap;

	public MapIterator(AbstractHashMap<WrappedK, K, V> hashMap, IMapEntry<K, V>[] table, boolean removeAllowed)
	{
		super(removeAllowed);
		this.hashMap = hashMap;
		this.table = table;
		nextEntry = getNextBucketFromIndex(table.length);
	}

	protected IMapEntry<K, V> getNextBucketFromIndex(int index)
	{
		IMapEntry<K, V>[] table = this.table;
		while (index-- > 0)
		{
			IMapEntry<K, V> entry = table[index];
			if (entry != null)
			{
				this.index = index;
				return entry;
			}
		}
		return null;
	}

	@Override
	public boolean hasNext()
	{
		return nextEntry != null;
	}

	@Override
	public Entry<K, V> next()
	{
		IMapEntry<K, V> currEntry = nextEntry;
		IMapEntry<K, V> nextEntry = currEntry != null ? currEntry.getNextEntry() : null;
		if (nextEntry != null)
		{
			this.nextEntry = nextEntry;
		}
		else
		{
			this.nextEntry = getNextBucketFromIndex(index);
		}
		return currEntry;
	}

	@Override
	public void remove()
	{
		if (!removeAllowed)
		{
			throw new UnsupportedOperationException();
		}
		hashMap.remove(currEntry.getKey());
		currEntry = currEntry.getNextEntry();
	}
}
