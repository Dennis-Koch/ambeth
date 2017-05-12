using System;
using System.Runtime.CompilerServices;
using System.Threading;

namespace De.Osthus.Ambeth.Event
{
    public class PausedEventTargetItem
    {
        public Object EventTarget { get; protected set; }

        protected Thread thread = Thread.CurrentThread;

        public int PauseCount { get; set; }
        
        public PausedEventTargetItem(Object eventTarget)
        {
            this.EventTarget = eventTarget;
        }

        public Thread Thread
        {
            get
            {
                return thread;
            }
        }
        
        public override int GetHashCode()
        {
            return 11 ^ RuntimeHelpers.GetHashCode(EventTarget);
        }

        public override bool Equals(object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (!(obj is PausedEventTargetItem))
            {
                return false;
            }
            PausedEventTargetItem other = (PausedEventTargetItem)obj;
            return Object.ReferenceEquals(EventTarget, other.EventTarget);
        }
    }
}
