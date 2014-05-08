using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Event.Model;

namespace De.Osthus.Ambeth.Event.Transfer
{
    [DataContract(Name = "EventItem", Namespace = "http://schemas.osthus.de/Ambeth")]
	public class EventItem : IEventItem
	{
        [DataMember]
        public Object EventObject { get; set; }

        [DataMember]
        public long SequenceNumber { get; set; }

        [DataMember]
        public DateTime DispatchTime { get; set; }

        public EventItem()
        {
            // Intended blank
        }
        
        public EventItem(Object eventObject, long sequenceNumber)
        {
            this.EventObject = eventObject;
            this.SequenceNumber = sequenceNumber;
            DispatchTime = DateTime.Now;
        }
	}
}
