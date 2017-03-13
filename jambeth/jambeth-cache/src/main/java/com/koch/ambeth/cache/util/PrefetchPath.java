package com.koch.ambeth.cache.util;

public class PrefetchPath
{
	public final int memberIndex;

	public final String memberName;

	public final Class<?> memberType;

	public final PrefetchPath[] children;

	public final Class<?>[] memberTypesOnDescendants;

	public PrefetchPath(Class<?> memberType, int memberIndex, String memberName, PrefetchPath[] children, Class<?>[] memberTypesOnDescendants)
	{
		this.memberIndex = memberIndex;
		this.memberName = memberName;
		this.memberType = memberType;
		this.children = children;
		this.memberTypesOnDescendants = memberTypesOnDescendants;
	}

	@Override
	public String toString()
	{
		return memberName;
	}
}
