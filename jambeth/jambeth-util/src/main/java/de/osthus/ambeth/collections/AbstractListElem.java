package de.osthus.ambeth.collections;

public class AbstractListElem<V>
{
	protected V next, prev;

	public V getNext()
	{
		return next;
	}

	public V getPrev()
	{
		return prev;
	}
}
