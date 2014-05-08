using System;

namespace De.Osthus.Ambeth.Mapping
{
    public interface IDedicatedMapperExtendable
    {
        void RegisterDedicatedMapper(IDedicatedMapper dedicatedMapper, Type entityType);

        void UnregisterDedicatedMapper(IDedicatedMapper dedicatedMapper, Type entityType);
    }
}