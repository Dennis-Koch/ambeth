package de.osthus.ambeth.cache;

import de.osthus.ambeth.collections.IList;

public interface IFirstLevelCacheManager
{
	IList<IWritableCache> selectFirstLevelCaches();
}