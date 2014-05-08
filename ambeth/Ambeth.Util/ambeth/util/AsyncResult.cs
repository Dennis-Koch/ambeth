using System;
using System.Net;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
    public class AsyncResult : IAsyncResult
    {
        public AsyncResult(Object asyncState)
        {
            this.AsyncState = asyncState;
        }

        public object AsyncState { get; private set;}

        public WaitHandle AsyncWaitHandle { get; private set; }

        public bool CompletedSynchronously { get { return true; } }

        public bool IsCompleted { get { return true; } }
    }
}
