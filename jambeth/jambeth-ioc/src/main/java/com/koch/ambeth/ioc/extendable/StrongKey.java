package com.koch.ambeth.ioc.extendable;

public class StrongKey<V>
{
	protected final V extension;

	protected final Class<?> strongType;

	public StrongKey(V extension, Class<?> strongType)
	{
		this.extension = extension;
		this.strongType = strongType;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof StrongKey))
		{
			return false;
		}
		StrongKey<?> other = (StrongKey<?>) obj;
		return extension == other.extension && strongType.equals(other.strongType);
	}

	@Override
	public int hashCode()
	{
		return extension.hashCode() ^ strongType.hashCode();
	}

	@Override
	public String toString()
	{
		return "(Key: " + strongType.getName() + " Extension: " + extension.toString() + ")";
	}
}
