package de.osthus.ambeth.collections;

import de.osthus.ambeth.collections.LinkedIntKeySet.LinkedIntKeySetEntry;

public class LinkedIntSetIterator
{
	protected ListElem<? extends LinkedIntKeySetEntry> currPointer;

	protected FastList<? extends LinkedIntKeySetEntry> listToIterate;

	public void init(final FastList<? extends LinkedIntKeySetEntry> ilistToIterate)
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
		final int key = (currPointer.value).key;
		currPointer = currPointer.next;
		return key;
	}

	public final LinkedIntKeySetEntry nextEntry()
	{
		final LinkedIntKeySetEntry elem = currPointer.value;
		currPointer = currPointer.next;
		return elem;
	}
}
