using System.Threading;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Threading
{
    public class CyclicBarrier
    {
        private int m_remain;
        private EventWaitHandle m_event;

        public CyclicBarrier(int parties)
        {
            m_remain = parties;
            m_event = new ManualResetEvent(false);
        }

        public void Await()
        {
            // The last thread to signal also sets the event.
            if (Interlocked.Decrement(ref m_remain) == 0)
            {
                m_event.Set();
            }
            m_event.WaitOne();
        }

        public void Await(int millisecondsTimeout)
        {
            // The last thread to signal also sets the event.
            if (Interlocked.Decrement(ref m_remain) == 0)
            {
                m_event.Set();
            }
            m_event.WaitOne(millisecondsTimeout);
        }

        public void Await(TimeSpan timeout)
        {
            // The last thread to signal also sets the event.
            if (Interlocked.Decrement(ref m_remain) == 0)
            {
                m_event.Set();
            }
            m_event.WaitOne(timeout);
        }
    }
}
