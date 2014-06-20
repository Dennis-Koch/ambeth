package de.osthus.ambeth.accessor;

public abstract class AbstractAccessor
{
	protected AbstractAccessor(Class<?> type, String propertyName, Class<?> propertyType)
	{
		// Intended blank
	}

	public abstract boolean canRead();

	public abstract boolean canWrite();

	public abstract Object getValue(Object obj);

	public abstract void setValue(Object obj, Object value);
}
