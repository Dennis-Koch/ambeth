package com.koch.ambeth.persistence.api;

import com.koch.ambeth.util.IDisposable;

public interface ILinkCursor extends IDisposable
{
	boolean moveNext();

	ILinkCursorItem getCurrent();

	byte getFromIdIndex();

	byte getToIdIndex();
}
