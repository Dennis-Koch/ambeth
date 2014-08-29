using System;
using System.Net;

namespace De.Osthus.Ambeth.Event
{
    public interface IEventTargetListenerExtendable
    {
        void RegisterEventTargetListener(IEventTargetEventListener eventTargetListener, Type eventType);

        void UnregisterEventTargetListener(IEventTargetEventListener eventTargetListener, Type eventType);

        void RegisterEventTargetListener(IEventTargetEventListener eventTargetListener);

        void UnregisterEventTargetListener(IEventTargetEventListener eventTargetListener);
    }
}
