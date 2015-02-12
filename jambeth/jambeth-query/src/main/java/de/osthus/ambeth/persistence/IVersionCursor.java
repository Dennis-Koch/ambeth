package de.osthus.ambeth.persistence;

import de.osthus.ambeth.util.IDisposable;

public interface IVersionCursor extends IDisposable
{
	boolean moveNext();

	IVersionItem getCurrent();

	int getAlternateIdCount();
}
