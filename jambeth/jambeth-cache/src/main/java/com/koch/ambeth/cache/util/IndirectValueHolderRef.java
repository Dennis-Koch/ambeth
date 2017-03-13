package com.koch.ambeth.cache.util;

import com.koch.ambeth.cache.RootCache;
import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.merge.util.DirectValueHolderRef;
import com.koch.ambeth.service.metadata.RelationMember;

public class IndirectValueHolderRef extends DirectValueHolderRef
{
	protected final RootCache rootCache;

	public IndirectValueHolderRef(RootCacheValue cacheValue, RelationMember member, RootCache rootCache)
	{
		super(cacheValue, member);
		this.rootCache = rootCache;
	}

	public IndirectValueHolderRef(RootCacheValue cacheValue, RelationMember member, RootCache rootCache, boolean objRefsOnly)
	{
		super(cacheValue, member, objRefsOnly);
		this.rootCache = rootCache;
	}

	public RootCache getRootCache()
	{
		return rootCache;
	}
}
