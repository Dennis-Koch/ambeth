package com.koch.ambeth.cache.datachange;

import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.security.privilege.model.IPrivilege;

public class CacheValueAndPrivilege
{
	public final RootCacheValue cacheValue;

	public final IPrivilege privilege;

	public CacheValueAndPrivilege(RootCacheValue cacheValue, IPrivilege privilege)
	{
		this.cacheValue = cacheValue;
		this.privilege = privilege;
	}
}
