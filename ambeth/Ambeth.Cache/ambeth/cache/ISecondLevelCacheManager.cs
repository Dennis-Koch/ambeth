using System;

namespace De.Osthus.Ambeth.Cache
{
    public interface ISecondLevelCacheManager
    {
        IRootCache SelectSecondLevelCache();
    }
}