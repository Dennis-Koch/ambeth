package de.osthus.ambeth.util;

import de.osthus.ambeth.cache.RootCache;
import de.osthus.ambeth.cache.rootcachevalue.RootCacheValue;
import de.osthus.ambeth.metadata.RelationMember;

public class IndirectValueHolderRef extends DirectValueHolderRef
{
	protected final RootCache rootCache;

	public IndirectValueHolderRef(RootCacheValue cacheValue, RelationMember member, RootCache rootCache)
	{
		super(cacheValue, member);
		this.rootCache = rootCache;
	}

	public RootCache getRootCache()
	{
		return rootCache;
	}
}
