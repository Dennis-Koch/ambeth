package com.koch.ambeth.event;

import java.util.concurrent.CountDownLatch;

import com.koch.ambeth.util.IDisposable;

public class PausedEventTargetItem implements IDisposable
{
	protected final Object eventTarget;

	protected final Thread thread = Thread.currentThread();

	protected int pauseCount;

	protected volatile CountDownLatch latch;

	public PausedEventTargetItem(Object eventTarget)
	{
		this.eventTarget = eventTarget;
	}

	@Override
	public void dispose()
	{
		if (latch != null)
		{
			latch.countDown();
			latch = null;
		}
	}

	public CountDownLatch addLatch()
	{
		if (latch == null)
		{
			latch = new CountDownLatch(1);
		}
		return latch;
	}

	public void setLatch(CountDownLatch latch)
	{
		if (this.latch != null)
		{
			throw new IllegalStateException();
		}
		this.latch = latch;
	}

	public Object getEventTarget()
	{
		return eventTarget;
	}

	public int getPauseCount()
	{
		return pauseCount;
	}

	public void setPauseCount(int pauseCount)
	{
		this.pauseCount = pauseCount;
	}

	public Thread getThread()
	{
		return thread;
	}

	@Override
	public int hashCode()
	{
		return 11 ^ System.identityHashCode(eventTarget);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof PausedEventTargetItem))
		{
			return false;
		}
		PausedEventTargetItem other = (PausedEventTargetItem) obj;
		return eventTarget == other.eventTarget;
	}
}
