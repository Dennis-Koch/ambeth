using System;
using System.Net;

namespace De.Osthus.Ambeth.Event
{
    public interface IEventBatcherExtendable
    {
        void RegisterEventBatcher(IEventBatcher eventBatcher, Type eventType);

        void UnregisterEventBatcher(IEventBatcher eventBatcher, Type eventType);
    }
}
