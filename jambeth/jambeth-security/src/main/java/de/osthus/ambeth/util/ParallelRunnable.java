package de.osthus.ambeth.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.threadlocal.IForkState;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;

public class ParallelRunnable<R, V> implements Runnable
{
	protected final RunnableHandle<R, V> runnableHandle;

	protected final boolean buildThreadLocals;

	public ParallelRunnable(RunnableHandle<R, V> runnableHandle, boolean buildThreadLocals)
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
				public void invoke(V item) throws Throwable
				{
					R result = runnableHandle.run.invoke(item);
					IAggregrateResultHandler<R, V> aggregrateResultHandler = runnableHandle.aggregrateResultHandler;
					if (aggregrateResultHandler != null)
					{
						parallelLock.lock();
						try
						{
							aggregrateResultHandler.aggregateResult(result, item);
						}
						finally
						{
							parallelLock.unlock();
						}
					}
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
}