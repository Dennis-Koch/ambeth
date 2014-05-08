using System;
using System.Net;

namespace De.Osthus.Ambeth.Event
{
    public interface IQueuedEvent
    {
        Object EventObject { get; }

        DateTime DispatchTime { get; set; }

        long SequenceNumber { get; set; }
    }
}
