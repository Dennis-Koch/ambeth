using System;

namespace De.Osthus.Ambeth.Cache.Collections
{
    public interface ICacheMapEntryFactory
    {
	    CacheMapEntry CreateCacheMapEntry(Type entityType, sbyte idIndex, Object id, Object value, CacheMapEntry nextEntry);
    }
}
