package de.osthus.ambeth.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;

public class MultithreadingHelper implements IMultithreadingHelper
{
	public static final String TIMEOUT = "ambeth.mth.timeout";

	@Autowired
	protected IServiceContext beanContext;

	@Property(name = TIMEOUT, defaultValue = "30000")
	protected long timeout;

	@Override
	public void invokeInParallel(IServiceContext serviceContext, final Runnable runnable, int workerCount)
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
		invokeInParallel(serviceContext, runnables);
	}

	@Override
	public void invokeInParallel(final IServiceContext serviceContext, Runnable... runnables)
	{
		final CountDownLatch latch = new CountDownLatch(runnables.length);

		final ParamHolder<Throwable> throwableHolder = new ParamHolder<Throwable>();

		Thread[] threads = new Thread[runnables.length];
		for (int a = runnables.length; a-- > 0;)
		{
			Runnable catchingRunnable = beanContext.registerBean(CatchingRunnable.class)//
					.propertyValue("Runnable", runnables[a])//
					.propertyValue("Latch", latch)//
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
}
