package com.koch.ambeth.merge.server.service;

public class CheckForPreviousParentKey
{
	protected final Class<?> entityType;

	protected final String memberName;

	public CheckForPreviousParentKey(Class<?> entityType, String memberName)
	{
		this.entityType = entityType;
		this.memberName = memberName;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof CheckForPreviousParentKey))
		{
			return false;
		}
		CheckForPreviousParentKey other = (CheckForPreviousParentKey) obj;
		return entityType.equals(other.entityType) && memberName.equals(other.memberName);
	}

	@Override
	public int hashCode()
	{
		return entityType.hashCode() ^ memberName.hashCode();
	}

	@Override
	public String toString()
	{
		return entityType.getSimpleName() + "." + memberName;
	}
}
