package de.osthus.ambeth.cache.collections;

import de.osthus.ambeth.collections.AbstractIterator;

public class CacheMapIterator extends AbstractIterator<CacheMapEntry>
{
	protected CacheMapEntry currEntry, nextEntry;

	protected int index;

	protected final CacheMapEntry[] table;

	protected final CacheHashMap hashMap;

	public CacheMapIterator(CacheHashMap hashMap, CacheMapEntry[] table, boolean removeAllowed)
	{
		super(removeAllowed);
		this.hashMap = hashMap;
		this.table = table;
		nextEntry = getNextBucketFromIndex(table.length);
	}

	protected CacheMapEntry getNextBucketFromIndex(int index)
	{
		CacheMapEntry[] table = this.table;
		while (index-- > 0)
		{
			CacheMapEntry entry = table[index];
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
	public CacheMapEntry next()
	{
		CacheMapEntry currEntry = nextEntry;
		CacheMapEntry nextEntry = currEntry != null ? currEntry.getNextEntry() : null;
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
		hashMap.remove(currEntry.getEntityType(), currEntry.getIdIndex(), currEntry.getId());
		currEntry = currEntry.getNextEntry();
	}
}
