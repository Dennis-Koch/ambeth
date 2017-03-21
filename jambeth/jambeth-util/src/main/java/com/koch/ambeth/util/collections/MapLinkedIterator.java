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

import java.util.Map.Entry;

public class MapLinkedIterator<K, V> extends AbstractIterator<Entry<K, V>>
{
	protected MapLinkedEntry<K, V> currPointer, lastPointer;

	private final AbstractLinkedMap<K, V> hashMap;

	public MapLinkedIterator(final AbstractLinkedMap<K, V> hashMap, boolean removeAllowed)
	{
		super(removeAllowed);
		this.hashMap = hashMap;
		currPointer = hashMap.fastIterationList.getFirstElem();
	}

	@Override
	public final boolean hasNext()
	{
		return currPointer != null;
	}

	@Override
	public final MapLinkedEntry<K, V> next()
	{
		lastPointer = currPointer;
		currPointer = currPointer.next;
		return lastPointer;
	}

	@Override
	public void remove()
	{
		if (!removeAllowed)
		{
			throw new UnsupportedOperationException();
		}
		hashMap.remove(lastPointer.getKey());
		lastPointer = null;
	}
}
