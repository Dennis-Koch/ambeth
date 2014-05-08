using System;
using System.Net;

namespace De.Osthus.Ambeth.Event
{
    public interface IEventListener : IEventListenerMarker
    {
        void HandleEvent(Object eventObject, DateTime dispatchTime, long sequenceId);
    }
}
