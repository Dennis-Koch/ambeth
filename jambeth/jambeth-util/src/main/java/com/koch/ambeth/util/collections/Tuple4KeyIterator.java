package com.koch.ambeth.util.collections;

import java.util.Iterator;

public class Tuple4KeyIterator<Key1, Key2, Key3, Key4, V> implements Iterator<Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>>
{
	protected Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> currEntry, nextEntry;

	protected int index;

	protected final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table;

	private final AbstractTuple4KeyHashMap<Key1, Key2, Key3, Key4, V> hashMap;

	private final boolean removeAllowed;

	public Tuple4KeyIterator(AbstractTuple4KeyHashMap<Key1, Key2, Key3, Key4, V> hashMap, Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table,
			boolean removeAllowed)
	{
		this.hashMap = hashMap;
		this.table = table;
		this.removeAllowed = removeAllowed;
		nextEntry = getNextBucketFromIndex(table.length);
	}

	protected Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> getNextBucketFromIndex(int index)
	{
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>[] table = this.table;
		while (index-- > 0)
		{
			Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry = table[index];
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
	public Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> next()
	{
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> currEntry = nextEntry;
		Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> nextEntry = currEntry != null ? currEntry.getNextEntry() : null;
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
		hashMap.remove(currEntry.getKey1(), currEntry.getKey2(), currEntry.getKey3(), currEntry.getKey4());
		currEntry = currEntry.getNextEntry();
	}
}
