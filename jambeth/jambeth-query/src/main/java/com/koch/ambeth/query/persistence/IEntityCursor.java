package com.koch.ambeth.query.persistence;

import com.koch.ambeth.util.IDisposable;

public interface IEntityCursor<T> extends IDisposable
{
	boolean moveNext();

	T getCurrent();
}
