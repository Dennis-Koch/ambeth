package de.osthus.ambeth.util;

public class CachePath
{
	public final int memberIndex;

	public final String memberName;

	public final Class<?> memberType;

	public final CachePath[] children;

	public CachePath(Class<?> memberType, int memberIndex, String memberName, CachePath[] children)
	{
		this.memberIndex = memberIndex;
		this.memberName = memberName;
		this.memberType = memberType;
		this.children = children;
	}

	@Override
	public String toString()
	{
		return memberName;
	}
}
