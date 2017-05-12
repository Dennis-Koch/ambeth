using System;
using System.Net;
using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Event
{
    public interface IEventDispatcher : IEventQueue
    {
        void DispatchEvent(Object eventObject);

        void DispatchEvent(Object eventObject, DateTime dispatchTime, long sequenceId);

        void WaitEventToResume(Object eventTargetToResume, long maxWaitTime, IBackgroundWorkerParamDelegate<IProcessResumeItem> resumeDelegate, IBackgroundWorkerParamDelegate<Exception> errorDelegate);
    }
}
