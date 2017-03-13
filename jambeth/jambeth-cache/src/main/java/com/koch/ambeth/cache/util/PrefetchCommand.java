package com.koch.ambeth.cache.util;

import com.koch.ambeth.merge.util.DirectValueHolderRef;

public class PrefetchCommand
{
	public final DirectValueHolderRef valueHolder;

	public final PrefetchPath[] prefetchPaths;

	public PrefetchCommand(DirectValueHolderRef valueHolder, PrefetchPath[] cachePaths)
	{
		this.valueHolder = valueHolder;
		this.prefetchPaths = cachePaths;
	}

	@Override
	public int hashCode()
	{
		return valueHolder.hashCode() ^ prefetchPaths.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof PrefetchCommand))
		{
			return false;
		}
		PrefetchCommand other = (PrefetchCommand) obj;
		// Use equals() of ValueHolderKey
		return valueHolder.equals(other.valueHolder) && prefetchPaths == other.prefetchPaths;
	}
}
