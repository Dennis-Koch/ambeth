using System;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Event
{
    public class QueuedEvent : IQueuedEvent, IListElem<IQueuedEvent>
    {
        protected Object listHandle;

        public Object EventObject { get; private set; }

        public DateTime DispatchTime { get; set; }

        public long SequenceNumber { get; set; }

        public IListElem<IQueuedEvent> Prev { get; set; }

        public IListElem<IQueuedEvent> Next { get; set; }

        public object ListHandle
        {
            get
            {
                return listHandle;
            }
            set
            {
                if (listHandle != null && value != null)
                {
                    throw new ArgumentException();
                }
                listHandle = value;
            }
        }

        public IQueuedEvent ElemValue
        {
            get
            {
                return this;
            }
            set
            {
                throw new ArgumentException();
            }
        }

        public QueuedEvent(Object eventObject, DateTime dispatchTime, long sequenceId)
        {
            this.EventObject = eventObject;
            this.DispatchTime = dispatchTime;
            this.SequenceNumber = sequenceId;
        }
    }
}
