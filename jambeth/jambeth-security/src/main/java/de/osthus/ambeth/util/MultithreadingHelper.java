package de.osthus.ambeth.util;

import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IForkState;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;

public class MultithreadingHelper implements IMultithreadingHelper
{
	public static final String TIMEOUT = "ambeth.mth.timeout";

	@Autowired
	protected IServiceContext beanContext;

	@Autowired(optional = true)
	protected ExecutorService executor;

	@Autowired
	protected IThreadLocalCleanupController threadLocalCleanupController;

	@Property(name = TIMEOUT, defaultValue = "30000")
	protected long timeout;

	@Override
	public void invokeInParallel(IServiceContext serviceContext, Runnable runnable, int workerCount)
	{
		invokeInParallel(serviceContext, false, runnable, workerCount);
	}

	@Override
	public void invokeInParallel(IServiceContext serviceContext, boolean inheritThreadLocals, Runnable runnable, int workerCount)
	{
		Runnable[] runnables = new Runnable[workerCount];
		for (int a = workerCount; a-- > 0;)
		{
			if (runnable instanceof INamedRunnable)
			{
				String name = ((INamedRunnable) runnable).getName() + "-" + a;
				runnables[a] = new WrappingNamedRunnable(runnable, name);
			}
			else
			{
				runnables[a] = runnable;
			}
		}
		invokeInParallel(serviceContext, inheritThreadLocals, runnables);
	}

	@Override
	public void invokeInParallel(final IServiceContext serviceContext, Runnable... runnables)
	{
		invokeInParallel(serviceContext, false, runnables);
	}

	@Override
	public void invokeInParallel(IServiceContext serviceContext, boolean inheritThreadLocals, Runnable... runnables)
	{
		CountDownLatch latch = new CountDownLatch(runnables.length);
		ParamHolder<Throwable> throwableHolder = new ParamHolder<Throwable>();
		IForkState forkState = inheritThreadLocals ? threadLocalCleanupController.createForkState() : null;

		Thread[] threads = new Thread[runnables.length];
		for (int a = runnables.length; a-- > 0;)
		{
			Runnable catchingRunnable = beanContext.registerBean(CatchingRunnable.class)//
					.propertyValue("Runnable", runnables[a])//
					.propertyValue("Latch", latch)//
					.propertyValue("ForkState", forkState)//
					.propertyValue("ThrowableHolder", throwableHolder).finish();

			Thread thread = new Thread(catchingRunnable);
			thread.setContextClassLoader(Thread.currentThread().getContextClassLoader());
			thread.setDaemon(true);
			threads[a] = thread;
		}
		for (Thread thread : threads)
		{
			thread.start();
		}
		try
		{
			latch.await(timeout, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		if (throwableHolder.getValue() != null)
		{
			throw RuntimeExceptionUtil.mask(throwableHolder.getValue(), "Error occured while invoking runnables");
		}
	}

	@Override
	public <R, V> void invokeAndWait(IList<V> items, IResultingBackgroundWorkerParamDelegate<R, V> itemHandler,
			IAggregrateResultHandler<R, V> aggregateResultHandler)
	{
		if (items.size() == 0)
		{
			return;
		}
		ExecutorService executor = this.executor;
		if (items.size() == 1 || executor == null)
		{
			try
			{
				for (int a = items.size(); a-- > 0;)
				{
					V item = items.get(a);
					R result = itemHandler.invoke(item);
					if (aggregateResultHandler != null)
					{
						aggregateResultHandler.aggregateResult(result, item);
					}
				}
				return;
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		ResultingRunnableHandle<R, V> runnableHandle = new ResultingRunnableHandle<R, V>(itemHandler, aggregateResultHandler, new ArrayList<V>(items),
				threadLocalCleanupController);

		Runnable parallelRunnable = new ResultingParallelRunnable<R, V>(runnableHandle, true);
		Runnable mainRunnable = new ResultingParallelRunnable<R, V>(runnableHandle, false);

		QueueAndWait(items.size() - 1, parallelRunnable, mainRunnable, runnableHandle);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R, K, V> void invokeAndWait(IMap<K, V> items, IResultingBackgroundWorkerParamDelegate<R, Entry<K, V>> itemHandler,
			IAggregrateResultHandler<R, Entry<K, V>> aggregateResultHandler)
	{
		if (items.size() == 0)
		{
			return;
		}
		ExecutorService executor = this.executor;
		if (items.size() == 1 || executor == null)
		{
			try
			{
				for (Entry<K, V> item : items)
				{
					R result = itemHandler.invoke(item);
					if (aggregateResultHandler != null)
					{
						aggregateResultHandler.aggregateResult(result, item);
					}
				}
				return;
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		ArrayList<Entry<K, V>> itemsList = new ArrayList<Entry<K, V>>(items.size());
		for (Entry<K, V> item : items)
		{
			itemsList.add(item);
		}
		ResultingRunnableHandle<R, Entry<K, V>> runnableHandle = new ResultingRunnableHandle<R, Entry<K, V>>(itemHandler, aggregateResultHandler, itemsList,
				threadLocalCleanupController);

		Runnable parallelRunnable = new ResultingParallelRunnable<R, Entry<K, V>>(runnableHandle, true);
		Runnable mainRunnable = new ResultingParallelRunnable<R, Entry<K, V>>(runnableHandle, false);

		QueueAndWait(items.size() - 1, parallelRunnable, mainRunnable, runnableHandle);
	}

	@Override
	public <V> void invokeAndWait(IList<V> items, IBackgroundWorkerParamDelegate<V> itemHandler)
	{
		if (items.size() == 0)
		{
			return;
		}
		ExecutorService executor = this.executor;
		if (items.size() == 1 || executor == null)
		{
			try
			{
				for (int a = items.size(); a-- > 0;)
				{
					V item = items.get(a);
					itemHandler.invoke(item);
				}
				return;
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		RunnableHandle<V> runnableHandle = new RunnableHandle<V>(itemHandler, new ArrayList<V>(items), threadLocalCleanupController);

		Runnable parallelRunnable = new ParallelRunnable<V>(runnableHandle, true);
		Runnable mainRunnable = new ParallelRunnable<V>(runnableHandle, false);

		QueueAndWait(items.size() - 1, parallelRunnable, mainRunnable, runnableHandle);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V> void invokeAndWait(IMap<K, V> items, IBackgroundWorkerParamDelegate<Entry<K, V>> itemHandler)
	{
		if (items.size() == 0)
		{
			return;
		}
		ExecutorService executor = this.executor;
		if (items.size() == 1 || executor == null)
		{
			try
			{
				for (Entry<K, V> item : items)
				{
					itemHandler.invoke(item);
				}
				return;
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		ArrayList<Entry<K, V>> itemsList = new ArrayList<Entry<K, V>>(items.size());
		for (Entry<K, V> item : items)
		{
			itemsList.add(item);
		}
		RunnableHandle<Entry<K, V>> runnableHandle = new RunnableHandle<Entry<K, V>>(itemHandler, itemsList, threadLocalCleanupController);

		Runnable parallelRunnable = new ParallelRunnable<Entry<K, V>>(runnableHandle, true);
		Runnable mainRunnable = new ParallelRunnable<Entry<K, V>>(runnableHandle, false);

		QueueAndWait(items.size() - 1, parallelRunnable, mainRunnable, runnableHandle);
	}

	protected <V> void QueueAndWait(int forkCount, Runnable parallelRunnable, Runnable mainRunnable, AbstractRunnableHandle<V> runnableHandle)
	{
		// for n items fork at most n - 1 threads because our main thread behaves like a worker by itself
		for (int a = forkCount; a-- > 0;)
		{
			executor.execute(parallelRunnable);
		}
		// consume items with the "main thread" as long as there is one in the queue
		mainRunnable.run();

		// wait till the forked threads have finished, too
		waitForLatch(runnableHandle.latch, runnableHandle.exHolder);
	}

	protected void waitForLatch(CountDownLatch latch, IParamHolder<Throwable> exHolder)
	{
		while (true)
		{
			if (exHolder.getValue() != null)
			{
				// A parallel exception will be thrown here

				Throwable ex = exHolder.getValue();
				throw RuntimeExceptionUtil.mask(ex);
			}
			try
			{
				latch.await();
				if (latch.getCount() == 0)
				{
					return;
				}
			}
			catch (InterruptedException e)
			{
				// intended blank
			}
		}
	}
}
