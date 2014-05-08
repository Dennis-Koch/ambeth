package de.osthus.ambeth.cache.collections;

public interface ICacheMapEntryFactory
{
	CacheMapEntry createCacheMapEntry(Class<?> entityType, byte idIndex, Object id, Object value, CacheMapEntry nextEntry);
}
