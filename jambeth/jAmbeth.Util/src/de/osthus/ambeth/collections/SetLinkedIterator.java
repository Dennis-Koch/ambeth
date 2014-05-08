package de.osthus.ambeth.collections;

public class SetLinkedIterator<K> extends AbstractIterator<K>
{
	protected SetLinkedEntry<K> currPointer, lastPointer;

	private final AbstractLinkedSet<K> hashSet;

	public SetLinkedIterator(final AbstractLinkedSet<K> hashSet, boolean removeAllowed)
	{
		super(removeAllowed);
		this.hashSet = hashSet;
		currPointer = hashSet.fastIterationList.getFirstElem();
		lastPointer = null;
	}

	@Override
	public final boolean hasNext()
	{
		return currPointer != null;
	}

	@Override
	public final K next()
	{
		final K elem = currPointer.key;
		lastPointer = currPointer;
		currPointer = currPointer.next;
		return elem;
	}

	@Override
	public void remove()
	{
		if (!removeAllowed)
		{
			throw new UnsupportedOperationException();
		}
		hashSet.remove(lastPointer.key);
		lastPointer = null;
	}
}
