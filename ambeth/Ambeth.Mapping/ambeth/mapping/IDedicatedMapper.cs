using System;

namespace De.Osthus.Ambeth.Mapping
{
    public interface IDedicatedMapper
    {
        void ApplySpecialMapping(Object businessObject, Object valueObject, CopyDirection direction);
    }
}
