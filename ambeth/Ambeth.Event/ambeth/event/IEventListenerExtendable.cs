using System;
using System.Net;

namespace De.Osthus.Ambeth.Event
{
    public interface IEventListenerExtendable
    {
        void RegisterEventListener(IEventListenerMarker eventListener, Type eventType);

        void UnregisterEventListener(IEventListenerMarker eventListener, Type eventType);

        void RegisterEventListener(IEventListenerMarker eventListener);

        void UnregisterEventListener(IEventListenerMarker eventListener);
    }
}
