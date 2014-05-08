package de.osthus.ambeth.util;

import java.util.concurrent.CountDownLatch;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class MultithreadingHelper
{
	@SuppressWarnings("unused")
	@LogInstance(MultithreadingHelper.class)
	private ILogger log;

	public static void invokeInParallel(IServiceContext serviceContext, Runnable runnable, int workerCount)
	{
		Runnable[] runnables = new Runnable[workerCount];
		for (int a = workerCount; a-- > 0;)
		{
			runnables[a] = runnable;
		}
		invokeInParallel(serviceContext, runnables);
	}

	public static void invokeInParallel(final IServiceContext serviceContext, Runnable... runnables)
	{
		final CountDownLatch latch = new CountDownLatch(runnables.length);

		final ParamHolder<Throwable> throwableHolder = new ParamHolder<Throwable>();

		final IThreadLocalCleanupController threadLocalCleanupController;
		if (serviceContext != null)
		{
			threadLocalCleanupController = serviceContext.getService(IThreadLocalCleanupController.class);
		}
		else
		{
			threadLocalCleanupController = null;
		}

		Thread[] threads = new Thread[runnables.length];
		for (int a = runnables.length; a-- > 0;)
		{
			final Runnable runnable = runnables[a];
			Runnable catchingRunnable = new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						runnable.run();
					}
					catch (Throwable e)
					{
						throwableHolder.setValue(e);
						throw RuntimeExceptionUtil.mask(e);
					}
					finally
					{
						if (threadLocalCleanupController != null)
						{
							threadLocalCleanupController.cleanupThreadLocal();
						}
						latch.countDown();
					}
				}
			};
			threads[a] = new Thread(catchingRunnable);
		}
		for (Thread thread : threads)
		{
			thread.start();
		}
		try
		{
			latch.await();
		}
		catch (InterruptedException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		if (throwableHolder.getValue() != null)
		{
			throw RuntimeExceptionUtil.mask(throwableHolder.getValue());
		}
	}
}
