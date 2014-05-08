using System;

namespace De.Osthus.Ambeth.Util
{
    public interface IPrefetchConfig
    {
        IPrefetchConfig Add(Type entityType, String propertyPath);

        IPrefetchHandle Build();
    }
}