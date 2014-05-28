package de.osthus.ambeth.persistence;

import de.osthus.ambeth.util.IDisposable;

public interface IEntityCursor<T> extends IDisposable
{
	boolean moveNext();

	T getCurrent();
}
