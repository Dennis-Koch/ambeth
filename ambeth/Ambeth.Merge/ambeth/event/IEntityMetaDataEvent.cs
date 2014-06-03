using System;

namespace De.Osthus.Ambeth.Event
{
    public interface IEntityMetaDataEvent
    {
        Type[] EntityTypes { get; }
    }
}
