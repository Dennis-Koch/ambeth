package de.osthus.ambeth.util;

import de.osthus.ambeth.collections.IList;

public class CachePath
{
	public final int memberIndex;

	public final String memberName;

	public final Class<?> memberType;

	public IList<CachePath> children;

	public CachePath(Class<?> memberType, int memberIndex, String memberName)
	{
		this.memberIndex = memberIndex;
		this.memberName = memberName;
		this.memberType = memberType;
	}
}
