package com.koch.ambeth.query.persistence;

import com.koch.ambeth.util.IDisposable;

public interface IDataCursor extends IDisposable
{
	boolean moveNext();

	IDataItem getCurrent();

	int getFieldCount();
}
