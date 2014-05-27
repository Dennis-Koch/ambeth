package de.osthus.ambeth.collections;

import java.util.Iterator;

public class Tuple2KeyIterator<Key1, Key2, V> implements Iterator<Tuple2KeyEntry<Key1, Key2, V>>
{
	protected Tuple2KeyEntry<Key1, Key2, V> currEntry, nextEntry;

	protected int index;

	protected final Tuple2KeyEntry<Key1, Key2, V>[] table;

	private final AbstractTuple2KeyHashMap<Key1, Key2, V> hashMap;

	private final boolean removeAllowed;

	public Tuple2KeyIterator(AbstractTuple2KeyHashMap<Key1, Key2, V> hashMap, Tuple2KeyEntry<Key1, Key2, V>[] table, boolean removeAllowed)
	{
		this.hashMap = hashMap;
		this.table = table;
		this.removeAllowed = removeAllowed;
		nextEntry = getNextBucketFromIndex(table.length);
	}

	protected Tuple2KeyEntry<Key1, Key2, V> getNextBucketFromIndex(int index)
	{
		Tuple2KeyEntry<Key1, Key2, V>[] table = this.table;
		while (index-- > 0)
		{
			Tuple2KeyEntry<Key1, Key2, V> entry = table[index];
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
	public Tuple2KeyEntry<Key1, Key2, V> next()
	{
		Tuple2KeyEntry<Key1, Key2, V> currEntry = nextEntry;
		Tuple2KeyEntry<Key1, Key2, V> nextEntry = currEntry != null ? currEntry.getNextEntry() : null;
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
		hashMap.remove(currEntry.getKey1(), currEntry.getKey2());
		currEntry = currEntry.getNextEntry();
	}
}
