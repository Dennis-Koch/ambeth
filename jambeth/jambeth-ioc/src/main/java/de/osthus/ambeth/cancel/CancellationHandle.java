package de.osthus.ambeth.cancel;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.WeakHashSet;

public class CancellationHandle implements ICancellationHandle
{
	protected volatile boolean cancelled;

	protected final WeakHashSet<Thread> owningThreads = new WeakHashSet<Thread>();

	protected final Lock writeLock = new ReentrantLock();

	protected Cancellation cancellation;

	public CancellationHandle(Cancellation cancellation)
	{
		this.cancellation = cancellation;
	}

	public void addOwningThread()
	{
		writeLock.lock();
		try
		{
			owningThreads.add(Thread.currentThread());
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	@Override
	public void cancel()
	{
		cancelled = true;
		writeLock.lock();
		try
		{
			for (Thread owningThread : owningThreads)
			{
				if (owningThread != null)
				{
					owningThread.interrupt();
				}
			}
			owningThreads.clear();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void close() throws Exception
	{
		Cancellation cancellation = this.cancellation;
		if (cancellation == null)
		{
			return;
		}
		this.cancellation = null;
		ICancellationHandle cancellationHandle = cancellation.cancelledTL.get();
		if (cancellationHandle != this)
		{
			throw new IllegalStateException("Only the thread owning this cancellationHandle is allowed to close it");
		}
		cancellation.cancelledTL.set(null);
	}
}