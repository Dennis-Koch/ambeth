using System.Threading;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Threading
{
    public class CountDownLatch
    {
        private int m_remain;
        private EventWaitHandle m_event;

        public CountDownLatch(int count)
        {
            m_remain = count;
            m_event = new ManualResetEvent(false);
        }

        public int GetCount()
        {
            return m_remain;
        }

        public void CountDown()
        {
            // The last thread to signal also sets the event.
            if (Interlocked.Decrement(ref m_remain) == 0)
            {
                m_event.Set();
            }
        }

        public void Await()
        {
            m_event.WaitOne();
        }

        public void Await(int millisecondsTimeout)
        {
            m_event.WaitOne(millisecondsTimeout);
        }

        public void Await(TimeSpan timeout)
        {
            m_event.WaitOne(timeout);
        }
    }
}
