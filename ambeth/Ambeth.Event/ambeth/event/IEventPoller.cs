using System;
using System.Net;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Event
{
    public interface IEventPoller
    {
        void PausePolling();

        void ResumePolling();
    }
}
