package de.osthus.ambeth.collections;

import java.util.Iterator;

public class Tuple5KeyIterator<Key1, Key2, Key3, Key4, Key5, V> implements Iterator<Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V>>
{
	protected Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> currEntry, nextEntry;

	protected int index;

	protected final Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V>[] table;

	private final AbstractTuple5KeyHashMap<Key1, Key2, Key3, Key4, Key5, V> hashMap;

	private final boolean removeAllowed;

	public Tuple5KeyIterator(AbstractTuple5KeyHashMap<Key1, Key2, Key3, Key4, Key5, V> hashMap, Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V>[] table,
			boolean removeAllowed)
	{
		this.hashMap = hashMap;
		this.table = table;
		this.removeAllowed = removeAllowed;
		nextEntry = getNextBucketFromIndex(table.length);
	}

	protected Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> getNextBucketFromIndex(int index)
	{
		Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V>[] table = this.table;
		while (index-- > 0)
		{
			Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> entry = table[index];
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
	public Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> next()
	{
		Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> currEntry = nextEntry;
		Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> nextEntry = currEntry != null ? currEntry.getNextEntry() : null;
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
		hashMap.remove(currEntry.getKey1(), currEntry.getKey2(), currEntry.getKey3(), currEntry.getKey4(), currEntry.getKey5());
		currEntry = currEntry.getNextEntry();
	}
}
