package de.osthus.ambeth.cache.collections;

public class DefaultCacheMapEntryFactory implements ICacheMapEntryFactory
{
	@Override
	public CacheMapEntry createCacheMapEntry(Class<?> entityType, byte idIndex, Object id, Object value, CacheMapEntry nextEntry)
	{
		return new DefaultCacheMapEntry(entityType, idIndex, id, value, nextEntry);
	}
}
