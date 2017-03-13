package com.koch.ambeth.ioc.util;

import com.koch.ambeth.util.collections.IListElem;

public class Def2Entry<V> implements IListElem<Def2Entry<V>>, Comparable<Def2Entry<V>>
{
	private IListElem<Def2Entry<V>> prev, next;

	private Object listHandle;

	public final V extension;

	public final Class<?> sourceType, targetType;

	public final int sourceDistance;

	public final int targetDistance;

	public Def2Entry(V extension, Class<?> sourceType, Class<?> targetType, int sourceDistance, int targetDistance)
	{
		this.extension = extension;
		this.sourceType = sourceType;
		this.targetType = targetType;
		this.sourceDistance = sourceDistance;
		this.targetDistance = targetDistance;
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
	public IListElem<Def2Entry<V>> getPrev()
	{
		return prev;
	}

	@Override
	public void setPrev(IListElem<Def2Entry<V>> prev)
	{
		this.prev = prev;
	}

	@Override
	public IListElem<Def2Entry<V>> getNext()
	{
		return next;
	}

	@Override
	public void setNext(IListElem<Def2Entry<V>> next)
	{
		this.next = next;
	}

	@Override
	public Def2Entry<V> getElemValue()
	{
		return this;
	}

	@Override
	public void setElemValue(Def2Entry<V> value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(Def2Entry<V> o)
	{
		if (o.sourceDistance > sourceDistance)
		{
			return 1;
		}
		if (o.sourceDistance == sourceDistance)
		{
			if (o.targetDistance > targetDistance)
			{
				return 1;
			}
			if (o.targetDistance == targetDistance)
			{
				return 0;
			}
		}
		return -1;
	}
}