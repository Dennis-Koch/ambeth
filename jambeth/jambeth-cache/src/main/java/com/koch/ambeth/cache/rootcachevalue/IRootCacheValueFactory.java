package com.koch.ambeth.cache.rootcachevalue;

import com.koch.ambeth.service.merge.model.IEntityMetaData;

public interface IRootCacheValueFactory
{
	RootCacheValue createRootCacheValue(IEntityMetaData metaData);
}
