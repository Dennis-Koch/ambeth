using System;

namespace De.Osthus.Ambeth.Orm
{
    public interface IOrmConfig
    {
        String Name { get; }

        bool ExplicitlyNotMergeRelevant { get; }
    }
}