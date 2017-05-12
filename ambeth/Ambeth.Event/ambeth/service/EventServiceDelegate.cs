using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Event.Model;
using De.Osthus.Ambeth.Event.Transfer;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Service
{
    public class EventServiceDelegate : IEventService, IInitializingBean
    {
        public virtual IEventServiceWCF EventServiceWCF { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(EventServiceWCF, "EventServiceWCF");
        }

        public virtual IList<IEventItem> PollEvents(long serverSession, long eventSequenceSince, TimeSpan requestedMaximumWaitTime)
        {
            EventItem[] resultWCF = EventServiceWCF.PollEvents(serverSession, eventSequenceSince, requestedMaximumWaitTime);
            List<IEventItem> result = new List<IEventItem>(resultWCF.Length);
            for (int a = 0, size = resultWCF.Length; a < size; a++)
            {
                result.Add(resultWCF[a]);
            }
            return result;
        }

        public virtual long GetCurrentEventSequence()
        {
            return EventServiceWCF.GetCurrentEventSequence();
        }

        public virtual long GetCurrentServerSession()
        {
            return EventServiceWCF.GetCurrentServerSession();
        }

        public long FindEventSequenceNumber(long time)
        {
            return EventServiceWCF.FindEventSequenceNumber(time);
        }
    }
}
