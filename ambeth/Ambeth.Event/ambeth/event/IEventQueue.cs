using System;
using System.Net;
using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Event
{
    public interface IEventQueue
    {
        void EnableEventQueue();

        void FlushEventQueue();

        void Pause(Object eventTarget);

        void Resume(Object eventTarget);

        T InvokeWithoutLocks<T>(IResultingBackgroundWorkerDelegate<T> runnable);
    }
}
