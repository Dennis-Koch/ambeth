package com.koch.ambeth.query.persistence;

import com.koch.ambeth.util.IDisposable;

public interface IDataItem extends IDisposable
{
	Object getValue(String propertyName);

	Object getValue(int index);

	int getFieldCount();
}
