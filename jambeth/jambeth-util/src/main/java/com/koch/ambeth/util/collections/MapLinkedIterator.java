package com.koch.ambeth.util.collections;

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
