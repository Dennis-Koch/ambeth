package de.osthus.ambeth.persistence.parallel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.threadlocal.IForkState;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.ParamHolder;

public class ParallelRunnable<V> implements Runnable
{
	protected final RunnableHandle<V> runnableHandle;

	protected final boolean buildThreadLocals;

	public ParallelRunnable(RunnableHandle<V> runnableHandle, boolean buildThreadLocals)
	{
		this.runnableHandle = runnableHandle;
		this.buildThreadLocals = buildThreadLocals;
	}

	@Override
	public void run()
	{
		try
		{
			final Lock parallelLock = runnableHandle.parallelLock;
			IList<V> items = runnableHandle.items;
			IForkState forkState = runnableHandle.forkState;
			ParamHolder<Throwable> exHolder = runnableHandle.exHolder;
			CountDownLatch latch = runnableHandle.latch;

			IBackgroundWorkerParamDelegate<V> run = new IBackgroundWorkerParamDelegate<V>()
			{
				@Override
				public void invoke(V state) throws Throwable
				{
					runnableHandle.run.invoke(state);
					writeParallelResult(parallelLock, state);
				}
			};

			while (true)
			{
				V item;
				parallelLock.lock();
				try
				{
					if (exHolder.getValue() != null)
					{
						// an uncatched error occurred somewhere
						return;
					}
					// pop the last item of the queue
					item = items.popLastElement();
				}
				finally
				{
					parallelLock.unlock();
				}
				if (item == null)
				{
					// queue finished
					return;
				}
				try
				{
					if (buildThreadLocals)
					{
						forkState.use(run, item);
					}
					else
					{
						run.invoke(item);
					}
				}
				catch (Throwable e)
				{
					parallelLock.lock();
					try
					{
						if (exHolder.getValue() == null)
						{
							exHolder.setValue(e);
						}
					}
					finally
					{
						parallelLock.unlock();
					}
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
				runnableHandle.threadLocalCleanupController.cleanupThreadLocal();
			}
		}
	}

	protected void writeParallelResult(Lock parallelLock, V item)
	{
		// intended blank
	}
}