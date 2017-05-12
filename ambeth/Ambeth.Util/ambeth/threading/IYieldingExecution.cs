using System.Threading;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Threading
{
    public interface IYieldingExecution
    {
        void Queue<T>(long yieldingInterval, ExecuteYieldingDelegate<T> executeYieldingDelegate, T item);
    }
}
