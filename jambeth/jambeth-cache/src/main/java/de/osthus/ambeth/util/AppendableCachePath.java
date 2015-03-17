package de.osthus.ambeth.util;

import de.osthus.ambeth.collections.ISet;

public class AppendableCachePath
{
	public final int memberIndex;

	public final String memberName;

	public final Class<?> memberType;

	public ISet<AppendableCachePath> children;

	public AppendableCachePath(Class<?> memberType, int memberIndex, String memberName)
	{
		this.memberIndex = memberIndex;
		this.memberName = memberName;
		this.memberType = memberType;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof AppendableCachePath))
		{
			return false;
		}
		return memberIndex == ((AppendableCachePath) obj).memberIndex;
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ memberIndex;
	}

	@Override
	public String toString()
	{
		return memberName;
	}
}
