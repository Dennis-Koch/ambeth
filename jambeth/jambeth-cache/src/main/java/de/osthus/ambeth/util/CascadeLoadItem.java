package de.osthus.ambeth.util;


public class CascadeLoadItem
{
	public final Class<?> realType;

	public final DirectValueHolderRef valueHolder;

	public final CachePath[] cachePaths;

	public CascadeLoadItem(Class<?> realType, DirectValueHolderRef valueHolder, CachePath[] cachePaths)
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
		// Use equals() of ValueHolderKey
		return valueHolder.equals(other.valueHolder) && cachePaths == other.cachePaths;
	}

}
