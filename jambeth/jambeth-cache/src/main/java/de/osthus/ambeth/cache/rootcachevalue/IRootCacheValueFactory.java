package de.osthus.ambeth.cache.rootcachevalue;

import de.osthus.ambeth.merge.model.IEntityMetaData;

public interface IRootCacheValueFactory
{
	RootCacheValue createRootCacheValue(IEntityMetaData metaData);
}
