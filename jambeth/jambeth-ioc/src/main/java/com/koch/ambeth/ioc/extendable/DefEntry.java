package com.koch.ambeth.ioc.extendable;

import com.koch.ambeth.util.collections.IListElem;

public class DefEntry<V> implements IListElem<DefEntry<V>>, Comparable<DefEntry<V>>
{
	protected IListElem<DefEntry<V>> prev, next;

	protected Object listHandle;

	public final V extension;

	public final Class<?> type;

	public final int distance;

	public DefEntry(V extension, Class<?> type, int distance)
	{
		this.extension = extension;
		this.type = type;
		this.distance = distance;
	}

	@Override
	public Object getListHandle()
	{
		return listHandle;
	}

	@Override
	public void setListHandle(Object listHandle)
	{
		this.listHandle = listHandle;
	}

	@Override
	public IListElem<DefEntry<V>> getPrev()
	{
		return prev;
	}

	@Override
	public void setPrev(IListElem<DefEntry<V>> prev)
	{
		this.prev = prev;
	}

	@Override
	public IListElem<DefEntry<V>> getNext()
	{
		return next;
	}

	@Override
	public void setNext(IListElem<DefEntry<V>> next)
	{
		this.next = next;
	}

	@Override
	public DefEntry<V> getElemValue()
	{
		return this;
	}

	@Override
	public void setElemValue(DefEntry<V> value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(DefEntry<V> o)
	{
		if (o.distance > distance)
		{
			return 1;
		}
		if (o.distance == distance)
		{
			return 0;
		}
		return -1;
	}
}