package de.osthus.ambeth.persistence;

import de.osthus.ambeth.util.IDisposable;

public interface ILinkCursor extends IDisposable
{
	boolean moveNext();

	ILinkCursorItem getCurrent();

	byte getFromIdIndex();

	byte getToIdIndex();
}
