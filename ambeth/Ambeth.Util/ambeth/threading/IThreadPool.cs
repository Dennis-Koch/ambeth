using System.Threading;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Threading
{
    public interface IThreadPool : IDelayedExecution, IYieldingExecution
    {
        void Queue(IBackgroundWorkerDelegate workerRunnable);

        void Queue<T>(IBackgroundWorkerParamDelegate<T> workerRunnable, T state);

        void Queue(WaitCallback waitCallback, Object state);
    }
}
