package com.koch.ambeth.util.threading;

/*-
 * #%L
 * jambeth-util
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.concurrent.CountDownLatch;

import com.koch.ambeth.util.collections.FastList;
import com.koch.ambeth.util.collections.ListElem;

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
						Thread currentThread = Thread.currentThread();
						ClassLoader oldContextClassLoader = currentThread.getContextClassLoader();
						currentThread.setContextClassLoader(queueItem.getContextClassLoader());
						try
						{
							((Runnable) queueItem.getObject()).run();
						}
						finally
						{
							currentThread.setContextClassLoader(oldContextClassLoader);
						}
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
