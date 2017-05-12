using System;
using System.Net;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Event
{
    public interface IEventTargetEventListener : IEventListenerMarker
    {
        void HandleEvent(Object eventObject, Object resumedEventTarget, IList<Object> pausedEventTargets, DateTime dispatchTime, long sequenceId);
    }
}
