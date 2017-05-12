using System;

namespace De.Osthus.Ambeth.Mapping
{
    public interface IDedicatedMapperRegistry
    {
        IDedicatedMapper GetDedicatedMapper(Type entityType);
    }
}
