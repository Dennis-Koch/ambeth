package de.osthus.ambeth.collections;

public abstract class ListCompare
{
	protected int id = -1;

	public void setFindID(final int iid)
	{
		id = iid;
	}

	public boolean isObject(final Object value)
	{
		return false;
	}

	public void handleObject(final Object value)
	{

	}
}
