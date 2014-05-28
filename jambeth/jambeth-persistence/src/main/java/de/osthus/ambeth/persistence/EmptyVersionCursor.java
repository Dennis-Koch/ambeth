package de.osthus.ambeth.persistence;

import de.osthus.ambeth.util.IDisposable;

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
	public byte getAlternateIdCount()
	{
		return 0;
	}
}