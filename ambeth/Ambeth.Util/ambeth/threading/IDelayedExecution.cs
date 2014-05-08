using System.Threading;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Threading
{
    public interface IDelayedExecution
    {
        void Queue<T>(QueueGroupKey<T> queueGroupKey, T item);

        void Queue<T>(QueueGroupKey<T> queueGroupKey, IEnumerable<T> items);
        
        void Queue(QueueGroupKey queueGroupKey);
    }
}
