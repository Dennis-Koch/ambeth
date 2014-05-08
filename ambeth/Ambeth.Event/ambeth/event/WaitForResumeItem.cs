using System;
using System.Net;
using System.Collections.Generic;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Event
{
    public class WaitForResumeItem : IProcessResumeItem
    {
        protected readonly IdentityHashSet<Object> pendingPauses;

	    protected readonly CountDownLatch latch = new CountDownLatch(1);

        protected readonly CountDownLatch resultLatch = new CountDownLatch(1);

        public WaitForResumeItem(IdentityHashSet<Object> pendingPauses)
	    {
		    this.pendingPauses = pendingPauses;
	    }

        public IdentityHashSet<Object> PendingPauses
        {
            get { return pendingPauses; }
	    }

	    public CountDownLatch Latch
	    {
            get { return latch; }
	    }

	    public CountDownLatch ResultLatch
	    {
            get { return resultLatch; }
	    }

	    public void ResumeProcessingFinished()
	    {
		    resultLatch.CountDown();
	    }
    }
}
