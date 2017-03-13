package com.koch.ambeth.query.jdbc;

import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.util.ParamChecker;

public class StringQueryKey implements IQueryKey
{
	protected final Class<?> entityType;

	protected final String value;

	public StringQueryKey(Class<?> entityType, String value)
	{
		ParamChecker.assertParamNotNull(entityType, "entityType");
		ParamChecker.assertParamNotNull(value, "value");
		this.entityType = entityType;
		this.value = value;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof StringQueryKey))
		{
			return false;
		}
		StringQueryKey other = (StringQueryKey) obj;

		return entityType.equals(other.entityType) && value.equals(other.value);
	}

	@Override
	public int hashCode()
	{
		return entityType.hashCode() ^ value.hashCode();
	}

	@Override
	public String toString()
	{
		return entityType.getName() + value;
	}
}
