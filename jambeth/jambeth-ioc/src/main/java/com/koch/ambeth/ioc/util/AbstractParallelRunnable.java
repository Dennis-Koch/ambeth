package com.koch.ambeth.ioc.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

public abstract class AbstractParallelRunnable<V> implements Runnable
{
	protected final boolean buildThreadLocals;

	private final AbstractRunnableHandle<V> abstractRunnableHandle;

	public AbstractParallelRunnable(AbstractRunnableHandle<V> abstractRunnableHandle, boolean buildThreadLocals)
	{
		this.abstractRunnableHandle = abstractRunnableHandle;
		this.buildThreadLocals = buildThreadLocals;
	}

	protected V retrieveNextItem()
	{
		AbstractRunnableHandle<V> runnableHandle = this.abstractRunnableHandle;
		Lock parallelLock = runnableHandle.parallelLock;
		parallelLock.lock();
		try
		{
			if (runnableHandle.getValue() != null)
			{
				// an uncatched error occurred somewhere
				return null;
			}
			// pop the last item of the queue
			return runnableHandle.items.popLastElement();
		}
		finally
		{
			parallelLock.unlock();
		}
	}

	protected void handleThrowable(Throwable e)
	{
		AbstractRunnableHandle<V> runnableHandle = this.abstractRunnableHandle;
		Lock parallelLock = runnableHandle.parallelLock;
		parallelLock.lock();
		try
		{
			if (runnableHandle.getValue() == null)
			{
				runnableHandle.setValue(e);
			}
		}
		finally
		{
			parallelLock.unlock();
		}
	}

	@Override
	public final void run()
	{
		Thread currentThread = Thread.currentThread();
		String oldName = currentThread.getName();
		if (buildThreadLocals)
		{
			String name = abstractRunnableHandle.createdThread.getName();
			currentThread.setName(name + " " + oldName);
		}
		try
		{
			CountDownLatch latch = abstractRunnableHandle.latch;

			while (true)
			{
				V item = retrieveNextItem();
				if (item == null)
				{
					// queue finished
					return;
				}
				try
				{
					runIntern(item);
				}
				catch (Throwable e)
				{
					handleThrowable(e);
				}
				finally
				{
					latch.countDown();
				}
			}
		}
		finally
		{
			if (buildThreadLocals)
			{
				currentThread.setName(oldName);
			}
			Thread.interrupted();
		}
	}

	protected abstract void runIntern(V item) throws Throwable;

}