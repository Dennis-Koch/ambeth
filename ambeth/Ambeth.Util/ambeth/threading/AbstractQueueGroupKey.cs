using System.Threading;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Threading
{
    public abstract class AbstractQueueGroupKey
    {
        public long QueueInterval { get; private set; }

        public bool InvokeFromGuiThread { get; private set; }
        
        public AbstractQueueGroupKey(long queueInterval)
            : this(queueInterval, false)
        {
            // Intended blank
        }

        public AbstractQueueGroupKey(long queueInterval, bool invokeFromGuiThread)
        {
            this.QueueInterval = queueInterval;
            this.InvokeFromGuiThread = invokeFromGuiThread;
        }
    }
}
