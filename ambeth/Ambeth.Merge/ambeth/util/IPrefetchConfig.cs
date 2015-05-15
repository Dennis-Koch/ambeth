using System;

namespace De.Osthus.Ambeth.Util
{
    public interface IPrefetchConfig
    {
        IPrefetchConfig Add(Type entityType, String propertyPath);

		IPrefetchConfig Add(Type entityType, params String[] propertyPaths);

        IPrefetchHandle Build();
    }
}