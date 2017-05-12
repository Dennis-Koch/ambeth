using System;

namespace De.Osthus.Ambeth.Cache.Collections
{
    public class DefaultCacheMapEntryFactory : ICacheMapEntryFactory
    {
	    public CacheMapEntry CreateCacheMapEntry(Type entityType, sbyte idIndex, Object id, Object value, CacheMapEntry nextEntry)
	    {
		    return new DefaultCacheMapEntry(entityType, idIndex, id, value, nextEntry);
	    }
    }
}
