package com.koch.ambeth.util.collections;

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
