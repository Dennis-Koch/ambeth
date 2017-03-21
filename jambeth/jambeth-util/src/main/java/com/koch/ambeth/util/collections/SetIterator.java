package com.koch.ambeth.util.collections;

/*-
 * #%L
 * jambeth-util
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

public class SetIterator<K> extends AbstractIterator<K>
{
	protected ISetEntry<K> currPointer, lastPointer;

	protected int tableIndex;

	protected final ISetEntry<K>[] table;

	protected final AbstractHashSet<K> hashSet;

	public SetIterator(final AbstractHashSet<K> hashSet, boolean removeAllowed)
	{
		super(removeAllowed);
		this.hashSet = hashSet;
		this.table = hashSet.table;

		tableIndex = -1;
		lastPointer = null;

		for (int a = table.length; a-- > 0;)
		{
			if (table[a] != null)
			{
				currPointer = table[a];
				tableIndex = a;
				break;
			}
		}
	}

	protected Object findNextPointer()
	{
		if (currPointer == null)
		{
			throw new IndexOutOfBoundsException();
		}
		ISetEntry<K> nextPointer = currPointer.getNextEntry();
		while (nextPointer == null)
		{
			if (tableIndex == 0)
			{
				break;
			}
			nextPointer = table[--tableIndex];
		}
		lastPointer = currPointer;
		currPointer = nextPointer;
		return currPointer;
	}

	@Override
	public final boolean hasNext()
	{
		return currPointer != null;
	}

	@Override
	public final K next()
	{
		findNextPointer();
		return lastPointer.getKey();
	}

	@Override
	public void remove()
	{
		if (!removeAllowed)
		{
			throw new UnsupportedOperationException();
		}
		hashSet.remove(lastPointer.getKey());
		lastPointer = null;
	}
}
