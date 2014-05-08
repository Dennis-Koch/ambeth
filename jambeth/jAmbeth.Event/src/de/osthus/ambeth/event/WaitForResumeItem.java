package de.osthus.ambeth.event;

import java.util.concurrent.CountDownLatch;

import de.osthus.ambeth.collections.IdentityHashSet;

public class WaitForResumeItem implements IProcessResumeItem
{
	protected final IdentityHashSet<Object> pendingPauses;

	protected final CountDownLatch latch = new CountDownLatch(1);

	protected final CountDownLatch resultLatch = new CountDownLatch(1);

	public WaitForResumeItem(IdentityHashSet<Object> pendingPauses)
	{
		this.pendingPauses = pendingPauses;
	}

	public IdentityHashSet<Object> getPendingPauses()
	{
		return pendingPauses;
	}

	public CountDownLatch getLatch()
	{
		return latch;
	}

	public CountDownLatch getResultLatch()
	{
		return resultLatch;
	}

	@Override
	public void resumeProcessingFinished()
	{
		resultLatch.countDown();
	}
}
