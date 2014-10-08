package de.osthus.ambeth.cache;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum CacheDirective
{
	None, FailEarly, ReturnMisses, NoResult, LoadContainerResult, CacheValueResult, FailInCacheHierarchy;

	private static Set<CacheDirective> cacheValueResultSet = EnumSet.of(CacheValueResult);

	private static Set<CacheDirective> loadContainerResultSet = EnumSet.of(LoadContainerResult);

	private static Set<CacheDirective> noResultSet = EnumSet.of(NoResult);

	private static Set<CacheDirective> returnMissesSet = EnumSet.of(ReturnMisses);

	private static Set<CacheDirective> failEarlySet = EnumSet.of(FailEarly);

	private static Set<CacheDirective> failEarlyAndReturnMissesSet = EnumSet.of(FailEarly, ReturnMisses);

	private static Set<CacheDirective> failInCacheHierarchySet = EnumSet.of(FailInCacheHierarchy);

	public static Set<CacheDirective> cacheValueResult()
	{
		return cacheValueResultSet;
	}

	public static Set<CacheDirective> failEarly()
	{
		return failEarlySet;
	}

	public static Set<CacheDirective> failEarlyAndReturnMisses()
	{
		return failEarlyAndReturnMissesSet;
	}

	public static Set<CacheDirective> failInCacheHierarchy()
	{
		return failInCacheHierarchySet;
	}

	public static Set<CacheDirective> loadContainerResult()
	{
		return loadContainerResultSet;
	}

	public static Set<CacheDirective> noResult()
	{
		return noResultSet;
	}

	public static Set<CacheDirective> returnMisses()
	{
		return returnMissesSet;
	}

	public static Set<CacheDirective> none()
	{
		return Collections.<CacheDirective> emptySet();
	}
}
