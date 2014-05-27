package de.osthus.ambeth.persistence;

import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.util.IDisposable;

public class EmptyLinkCursor implements IDisposable, ILinkCursor
{
	public static final ILinkCursor instance = new EmptyLinkCursor();

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
	public byte getFromIdIndex()
	{
		return ObjRef.UNDEFINED_KEY_INDEX;
	}

	@Override
	public byte getToIdIndex()
	{
		return ObjRef.UNDEFINED_KEY_INDEX;
	}

	@Override
	public ILinkCursorItem getCurrent()
	{
		return null;
	}

}