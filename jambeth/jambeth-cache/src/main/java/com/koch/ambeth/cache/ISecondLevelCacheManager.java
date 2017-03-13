package com.koch.ambeth.cache;

public interface ISecondLevelCacheManager
{
	IRootCache selectSecondLevelCache();

	IRootCache selectPrivilegedSecondLevelCache(boolean forceInstantiation);

	IRootCache selectNonPrivilegedSecondLevelCache(boolean forceInstantiation);
}