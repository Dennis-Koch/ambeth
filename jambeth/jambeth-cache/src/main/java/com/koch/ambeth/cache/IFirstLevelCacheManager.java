package com.koch.ambeth.cache;

import com.koch.ambeth.merge.cache.IWritableCache;
import com.koch.ambeth.util.collections.IList;

public interface IFirstLevelCacheManager
{
	IList<IWritableCache> selectFirstLevelCaches();
}