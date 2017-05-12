using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Cache.Collections
{
	public interface ICacheMapEntryTypeProvider
	{
		ICacheMapEntryFactory GetCacheMapEntryType(Type entityType, sbyte idIndex);
	}
}
