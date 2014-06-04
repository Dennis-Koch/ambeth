package de.osthus.ambeth.collections;

public class ListElem<V> implements IListElem<V>
{
	public ListElem<V> prev = null, next = null;

	protected Object listHandle;

	public V value;

	public ListElem()
	{
	}

	public ListElem(final V value)
	{
		this.value = value;
	}

	public void init(final V value)
	{
		this.value = value;
	}

	@Override
	public Object getListHandle()
	{
		return listHandle;
	}

	@Override
	public void setListHandle(Object listHandle)
	{
		if (this.listHandle != null && listHandle != null)
		{
			throw new UnsupportedOperationException();
		}
		this.listHandle = listHandle;
	}

	@Override
	public IListElem<V> getNext()
	{
		return next;
	}

	@Override
	public void setNext(IListElem<V> next)
	{
		this.next = (ListElem<V>) next;
	}

	@Override
	public IListElem<V> getPrev()
	{
		return next;
	}

	@Override
	public void setPrev(IListElem<V> prev)
	{
		this.prev = (ListElem<V>) prev;
	}

	@Override
	public V getElemValue()
	{
		return value;
	}

	@Override
	public void setElemValue(V value)
	{
		this.value = value;
	}
}