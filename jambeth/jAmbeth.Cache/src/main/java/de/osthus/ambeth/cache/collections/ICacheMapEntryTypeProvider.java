package de.osthus.ambeth.cache.collections;


public interface ICacheMapEntryTypeProvider
{
	ICacheMapEntryFactory getCacheMapEntryType(Class<?> entityType, byte idIndex);
}
