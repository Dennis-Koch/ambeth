package de.osthus.ambeth.cache;

import de.osthus.ambeth.privilege.model.IPrivilege;

public interface IRootCache extends ICache, ICacheIntern, IWritableCache
{
	boolean applyValues(Object targetObject, ICacheIntern targetCache, IPrivilege privilege);

	IRootCache getCurrentRootCache();

	IRootCache getParent();
}