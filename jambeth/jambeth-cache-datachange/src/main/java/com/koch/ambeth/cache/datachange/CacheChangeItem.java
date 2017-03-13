package com.koch.ambeth.cache.datachange;

import com.koch.ambeth.merge.cache.IWritableCache;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IList;

public class CacheChangeItem
{
	public IWritableCache cache;

	public IList<IObjRef> updatedObjRefs;

	public IList<IObjRef> deletedObjRefs;

	public IList<Object> updatedObjects;
}
