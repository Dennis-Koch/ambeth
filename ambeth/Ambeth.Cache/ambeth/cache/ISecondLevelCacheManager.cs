using System;

namespace De.Osthus.Ambeth.Cache
{
    public interface ISecondLevelCacheManager
    {
        IRootCache SelectSecondLevelCache();

        IRootCache SelectPrivilegedSecondLevelCache(bool forceInstantiation);

        IRootCache SelectNonPrivilegedSecondLevelCache(bool forceInstantiation);
    }
}