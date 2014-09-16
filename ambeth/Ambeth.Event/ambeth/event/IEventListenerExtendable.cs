using System;
using System.Net;

namespace De.Osthus.Ambeth.Event
{
    public interface IEventListenerExtendable
    {
        void RegisterEventListener(IEventListener eventListener, Type eventType);

	    void UnregisterEventListener(IEventListener eventListener, Type eventType);

	    void RegisterEventListener(IEventListener eventListener);

	    void UnregisterEventListener(IEventListener eventListener);
    }
}
