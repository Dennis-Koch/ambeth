package de.osthus.ambeth.persistence;

import de.osthus.ambeth.util.IDisposable;

public interface IDataCursor extends IDisposable
{
	boolean moveNext();

	IDataItem getCurrent();

	int getFieldCount();
}
