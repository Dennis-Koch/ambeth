using System;

namespace De.Osthus.Ambeth.Event.Store
{
    public interface IEventStoreHandlerExtendable
    {
        void RegisterEventStoreHandler(IEventStoreHandler eventStoreHandler, Type eventType);

        void UnregisterEventStoreHandler(IEventStoreHandler eventStoreHandler, Type eventType);
    }
}