package com.koch.ambeth.cache;

import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.IWritableCache;
import com.koch.ambeth.security.privilege.model.IPrivilege;

public interface IRootCache extends ICache, ICacheIntern, IWritableCache
{
	boolean applyValues(Object targetObject, ICacheIntern targetCache, IPrivilege privilege);

	IRootCache getCurrentRootCache();

	IRootCache getParent();
}