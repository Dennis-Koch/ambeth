package com.koch.ambeth.persistence;

import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.util.IDisposable;

public class EmptyVersionCursor implements IDisposable, IVersionCursor
{
	public static final IVersionCursor instance = new EmptyVersionCursor();

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
	public IVersionItem getCurrent()
	{
		return null;
	}

	@Override
	public int getAlternateIdCount()
	{
		return 0;
	}
}