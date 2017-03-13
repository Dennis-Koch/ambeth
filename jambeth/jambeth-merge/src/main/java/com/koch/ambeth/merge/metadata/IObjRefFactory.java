package com.koch.ambeth.merge.metadata;

import com.koch.ambeth.merge.cache.AbstractCacheValue;
import com.koch.ambeth.service.merge.model.IObjRef;

public abstract class IObjRefFactory
{
	public abstract IPreparedObjRefFactory prepareObjRefFactory(Class<?> entityType, int idIndex);

	public abstract IObjRef createObjRef(Class<?> entityType, int idIndex, Object id, Object version);

	public abstract IObjRef createObjRef(AbstractCacheValue cacheValue);

	public abstract IObjRef createObjRef(AbstractCacheValue cacheValue, int idIndex);

	public abstract IObjRef dup(IObjRef objRef);
}
