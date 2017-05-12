using System;

namespace De.Osthus.Ambeth.Event.Store
{
    public interface IEventStoreHandler
    {
        Object PostLoadFromStore(Object eventObject);

        Object PreSaveInStore(Object eventObject);

        void EventRemovedFromStore(Object eventObject);
    }
}
