using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Event.Model;

namespace De.Osthus.Ambeth.Service
{
    [XmlType]
    public interface IEventService
    {
        IList<IEventItem> PollEvents(long serverSession, long eventSequenceSince, TimeSpan requestedMaximumWaitTime);

        long GetCurrentEventSequence();

        long GetCurrentServerSession();

        long FindEventSequenceNumber(long time);
    }
}
