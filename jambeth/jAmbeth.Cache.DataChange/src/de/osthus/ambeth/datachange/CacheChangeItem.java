package de.osthus.ambeth.datachange;

import de.osthus.ambeth.cache.IWritableCache;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;

public class CacheChangeItem
{
	public IWritableCache cache;

	public IList<IObjRef> updatedObjRefs;

	public IList<IObjRef> deletedObjRefs;

	public IList<Object> updatedObjects;
}
