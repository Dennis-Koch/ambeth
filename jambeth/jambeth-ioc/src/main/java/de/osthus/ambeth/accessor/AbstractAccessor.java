package de.osthus.ambeth.accessor;

import de.osthus.ambeth.typeinfo.IPropertyInfo;

public abstract class AbstractAccessor
{
	protected AbstractAccessor(Class<?> type, IPropertyInfo property)
	{
		// Intended blank
	}

	public abstract boolean canRead();

	public abstract boolean canWrite();

	public abstract Object getValue(Object obj, boolean allowNullEquivalentValue);

	public abstract Object getValue(Object obj);

	public abstract void setValue(Object obj, Object value);

	public int getIntValue(Object obj)
	{
		return ((Number) getValue(obj, true)).intValue();
	}

	public void setIntValue(Object obj, int value)
	{
		setValue(obj, Integer.valueOf(value));
	}
}
