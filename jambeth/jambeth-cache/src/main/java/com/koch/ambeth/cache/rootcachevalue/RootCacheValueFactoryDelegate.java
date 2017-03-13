package com.koch.ambeth.cache.rootcachevalue;

import com.koch.ambeth.service.merge.model.IEntityMetaData;

public abstract class RootCacheValueFactoryDelegate
{
	public abstract RootCacheValue createRootCacheValue(IEntityMetaData metaData);
}
