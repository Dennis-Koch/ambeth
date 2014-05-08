package de.osthus.ambeth.proxy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class DefaultSet<T> extends HashSet<T> implements IDefaultCollection
{
	private static final long serialVersionUID = 8686103086497341659L;

	protected boolean hasDefaultState = true;

	@Override
	public boolean hasDefaultState()
	{
		return hasDefaultState;
	}

	@Override
	public boolean add(T e)
	{
		try
		{
			return super.add(e);
		}
		finally
		{
			hasDefaultState = false;
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		try
		{
			return super.addAll(c);
		}
		finally
		{
			hasDefaultState = false;
		}
	}

	@Override
	public void clear()
	{
		try
		{
			super.clear();
		}
		finally
		{
			hasDefaultState = false;
		}
	}

	@Override
	public Iterator<T> iterator()
	{
		try
		{
			return super.iterator();
		}
		finally
		{
			if (size() > 0)
			{
				hasDefaultState = false;
			}
		}
	}

	@Override
	public boolean remove(Object o)
	{
		try
		{
			return super.remove(o);
		}
		finally
		{
			hasDefaultState = false;
		}
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		try
		{
			return super.removeAll(c);
		}
		finally
		{
			hasDefaultState = false;
		}
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		try
		{
			return super.retainAll(c);
		}
		finally
		{
			hasDefaultState = false;
		}
	}
}