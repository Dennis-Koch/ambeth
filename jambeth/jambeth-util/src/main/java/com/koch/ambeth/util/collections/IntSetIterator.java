package com.koch.ambeth.util.collections;

import com.koch.ambeth.util.collections.IntKeySet.IntKeySetEntry;

public class IntSetIterator
{
	protected ListElem<? extends IntKeySetEntry> currPointer;

	protected FastList<? extends IntKeySetEntry> listToIterate;

	public void init(final FastList<? extends IntKeySetEntry> ilistToIterate)
	{
		listToIterate = ilistToIterate;
		currPointer = listToIterate.first();
	}

	public final boolean hasNext()
	{
		return currPointer != null;
	}

	public final int next()
	{
		final int key = (currPointer.value).getKey();
		currPointer = currPointer.next;
		return key;
	}

	public final IntKeySetEntry nextEntry()
	{
		final IntKeySetEntry elem = currPointer.value;
		currPointer = currPointer.next;
		return elem;
	}
}
