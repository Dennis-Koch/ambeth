using System;
using System.Net;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Event
{
    public interface IEventBatcher
    {
        IList<IQueuedEvent> BatchEvents(IList<IQueuedEvent> batchableEvents);
    }
}
