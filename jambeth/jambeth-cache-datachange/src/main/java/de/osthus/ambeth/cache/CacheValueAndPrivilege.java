package de.osthus.ambeth.cache;

import de.osthus.ambeth.cache.rootcachevalue.RootCacheValue;
import de.osthus.ambeth.privilege.model.IPrivilege;

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
