package de.osthus.ambeth.util;

import java.util.List;

public class CascadeLoadItem
{
	public final Class<?> realType;

	public final Object valueHolder;

	public final List<CachePath> cachePaths;

	public CascadeLoadItem(Class<?> realType, Object valueHolder, List<CachePath> cachePaths)
	{
		this.realType = realType;
		this.valueHolder = valueHolder;
		this.cachePaths = cachePaths;
	}

	@Override
	public int hashCode()
	{
		return valueHolder.hashCode() ^ cachePaths.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof CascadeLoadItem))
		{
			return false;
		}
		CascadeLoadItem other = (CascadeLoadItem) obj;
		if (valueHolder instanceof DirectValueHolderRef)
		{
			// Use equals() of ValueHolderKey
			return valueHolder.equals(other.valueHolder) && cachePaths == other.cachePaths;
		}
		return valueHolder == other.valueHolder && cachePaths == other.cachePaths;
	}

}
