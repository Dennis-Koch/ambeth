package de.osthus.ambeth.collections;

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
