package de.osthus.ambeth.collections;


public class IntKeyIterator<V>
{
	private LinkedIntKeyMapEntry<V> currPointer;

	private GenericFastList<LinkedIntKeyMapEntry<V>> listToIterate;

	public void reinit()
	{
		currPointer = listToIterate.getFirstElem();
	}

	public void init(final GenericFastList<LinkedIntKeyMapEntry<V>> ilistToIterate)
	{
		listToIterate = ilistToIterate;
		currPointer = listToIterate.getFirstElem();
	}

	public final boolean hasNext()
	{
		return currPointer != null;
	}

	public final V next()
	{
		try
		{
			return currPointer.value;
		}
		finally
		{
			currPointer = currPointer.next;
		}
	}

	public final LinkedIntKeyMapEntry<V> nextEntry()
	{
		try
		{
			return currPointer;
		}
		finally
		{
			currPointer = currPointer.next;
		}
	}
}
