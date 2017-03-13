package com.koch.ambeth.ioc.cancel;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.util.collections.WeakHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;

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

	public boolean addOwningThread()
	{
		writeLock.lock();
		try
		{
			return owningThreads.add(Thread.currentThread());
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public void removeOwningThread()
	{
		writeLock.lock();
		try
		{
			owningThreads.remove(Thread.currentThread());
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

	@Override
	public void withCancellationAwareness(IBackgroundWorkerDelegate runnable)
	{
		boolean hasBeenAdded = addOwningThread();
		try
		{
			runnable.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			if (hasBeenAdded)
			{
				removeOwningThread();
			}
		}
	}

	@Override
	public <R> R withCancellationAwareness(IResultingBackgroundWorkerDelegate<R> runnable)
	{
		boolean hasBeenAdded = addOwningThread();
		try
		{
			return runnable.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			if (hasBeenAdded)
			{
				removeOwningThread();
			}
		}
	}

	@Override
	public <R, V> R withCancellationAwareness(IResultingBackgroundWorkerParamDelegate<R, V> runnable, V state)
	{
		boolean hasBeenAdded = addOwningThread();
		try
		{
			return runnable.invoke(state);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			if (hasBeenAdded)
			{
				removeOwningThread();
			}
		}
	}
}