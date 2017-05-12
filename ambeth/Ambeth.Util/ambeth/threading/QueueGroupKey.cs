using System.Threading;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Threading
{
    public sealed class QueueGroupKey<T> : AbstractQueueGroupKey
    {
        public IBackgroundWorkerParamDelegate<IList<T>> QueuedDelegate { get; private set; }

        public QueueGroupKey(long queueInterval, IBackgroundWorkerParamDelegate<IList<T>> queuedDelegate)
            : this(queueInterval, false, queuedDelegate)
        {
            // Intended blank
        }

        public QueueGroupKey(long queueInterval, bool invokeFromGuiThread, IBackgroundWorkerParamDelegate<IList<T>> queuedDelegate) : base(queueInterval, invokeFromGuiThread)
        {
            this.QueuedDelegate = queuedDelegate;
        }
    }

    public sealed class QueueGroupKey : AbstractQueueGroupKey
    {
        public IBackgroundWorkerDelegate QueuedDelegate { get; private set; }

        public QueueGroupKey(long queueInterval, IBackgroundWorkerDelegate queuedDelegate)
            : this(queueInterval, false, queuedDelegate)
        {
            // Intended blank
        }

        public QueueGroupKey(long queueInterval, bool invokeFromGuiThread, IBackgroundWorkerDelegate queuedDelegate) : base(queueInterval, invokeFromGuiThread)
        {
            this.QueuedDelegate = queuedDelegate;
        }
    }
}
