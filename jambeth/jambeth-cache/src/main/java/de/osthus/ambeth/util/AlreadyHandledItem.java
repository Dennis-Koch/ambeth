package de.osthus.ambeth.util;


public class AlreadyHandledItem
{
	public final Object obj;

	public final CachePath[] cachePaths;

	public AlreadyHandledItem(Object obj, CachePath[] cachePaths)
	{
		this.obj = obj;
		this.cachePaths = cachePaths;
	}

	@Override
	public int hashCode()
	{
		if (cachePaths == null)
		{
			return System.identityHashCode(obj);
		}
		return System.identityHashCode(obj) ^ cachePaths.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof AlreadyHandledItem))
		{
			return false;
		}
		AlreadyHandledItem other = (AlreadyHandledItem) obj;
		return this.obj == other.obj && cachePaths == other.cachePaths;
	}

}
