using System;

namespace De.Osthus.Ambeth.Event.Store
{
    public interface IReplacedEvent
    {
        Type OriginalEventType { get; }
    }
}
