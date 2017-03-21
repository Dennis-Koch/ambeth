package com.koch.ambeth.cache.collections;

/*-
 * #%L
 * jambeth-cache
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.util.collections.AbstractIterator;

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
