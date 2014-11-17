package de.osthus.ambeth.cache.rootcachevalue;

import de.osthus.ambeth.merge.model.IEntityMetaData;

public abstract class RootCacheValueFactoryDelegate
{
	public abstract RootCacheValue createRootCacheValue(IEntityMetaData metaData);
}
