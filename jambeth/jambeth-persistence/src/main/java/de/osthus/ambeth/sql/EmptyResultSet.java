package de.osthus.ambeth.sql;

import de.osthus.ambeth.util.IDisposable;

public class EmptyResultSet implements IResultSet, IDisposable
{
	public static final IResultSet instance = new EmptyResultSet();

	@Override
	public void dispose()
	{
		// Intended blank
	}

	@Override
	public boolean moveNext()
	{
		return false;
	}

	@Override
	public Object[] getCurrent()
	{
		return null;
	}
}
