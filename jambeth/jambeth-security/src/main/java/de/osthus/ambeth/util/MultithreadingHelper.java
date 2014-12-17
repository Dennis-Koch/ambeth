package de.osthus.ambeth.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IForkState;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;

public class MultithreadingHelper implements IMultithreadingHelper
{
	public static final String TIMEOUT = "ambeth.mth.timeout";

	@Autowired
	protected IServiceContext beanContext;

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
}
