package de.osthus.ambeth.threading;

import java.util.concurrent.CountDownLatch;

import de.osthus.ambeth.collections.FastList;
import de.osthus.ambeth.collections.ListElem;

public class FastThreadPoolThread extends Thread
{
	private final ListElem<FastThreadPoolThread> freeLE = new ListElem<FastThreadPoolThread>(this);

	private FastList<FastThreadPoolThread> currentList;

	private final FastThreadPool asyncQueue;

	private int timeWithoutJob;

	private volatile boolean active = true;

	public FastThreadPoolThread(final FastThreadPool asyncQueue)
	{
		this.asyncQueue = asyncQueue;
		setDaemon(true);
	}

	public void queueOnList(final FastList<FastThreadPoolThread> currentList)
	{
		if (this.currentList != currentList)
		{
			if (this.currentList != null)
			{
				this.currentList.remove(freeLE);
			}
			this.currentList = currentList;
			if (this.currentList != null)
			{
				this.currentList.pushLast(freeLE);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run()
	{
		while (active)
		{
			Thread.interrupted(); // clear potential interrupted state
			try
			{
				QueueItem queueItem = asyncQueue.getNextMessage(this);
				if (queueItem != null)
				{
					timeWithoutJob = 0;
					HandlerRunnable<Object, Object> handler = ((HandlerRunnable<Object, Object>) queueItem.getHandler());
					CountDownLatch latch = queueItem.getLatch();
					if (handler == null)
					{
						((Runnable) queueItem.getObject()).run();
						if (latch != null)
						{
							latch.countDown();
						}
					}
					else
					{
						handler.handle(queueItem.getObject(), queueItem.getContext(), latch);
					}
					asyncQueue.actionFinished();
				}
			}
			catch (Throwable e)
			{
				asyncQueue.shutdownThread(this);
				e.printStackTrace(System.err);
			}
		}
	}

	public boolean isActive()
	{
		return active;
	}

	protected void setActive(boolean active)
	{
		this.active = active;
	}

	protected int getTimeWithoutJob()
	{
		return timeWithoutJob;
	}

	protected void setTimeWithoutJob(int timeWithoutJob)
	{
		this.timeWithoutJob = timeWithoutJob;
	}
}