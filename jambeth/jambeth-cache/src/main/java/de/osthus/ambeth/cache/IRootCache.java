package de.osthus.ambeth.cache;

public interface IRootCache extends ICache, ICacheIntern, IWritableCache
{
	boolean applyValues(Object targetObject, ICacheIntern targetCache);

	IRootCache getCurrentRootCache();

	IRootCache getParent();
}