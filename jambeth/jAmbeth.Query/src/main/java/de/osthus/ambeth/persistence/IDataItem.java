package de.osthus.ambeth.persistence;

import de.osthus.ambeth.util.IDisposable;

public interface IDataItem extends IDisposable
{
	Object getValue(String propertyName);

	Object getValue(int index);

	int getFieldCount();
}
