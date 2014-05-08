using System;
using System.Net;

namespace De.Osthus.Ambeth.Event
{
    public interface IEventListenerRegistry
    {
        void RegisterEventListener(IEventListener eventListener, params Type[] eventTypes);

        void UnregisterEventListener(IEventListener eventListener, params Type[] eventTypes);
    }
}
