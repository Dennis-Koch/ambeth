package de.osthus.ambeth.util;

import java.util.concurrent.CountDownLatch;

import de.osthus.ambeth.ioc.threadlocal.IForkState;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;

public class CatchingRunnable implements Runnable
{
	protected final IForkState forkState;

	protected final Runnable runnable;

	protected final CountDownLatch latch;

	protected final IParamHolder<Throwable> throwableHolder;

	protected final IThreadLocalCleanupController threadLocalCleanupController;

	public CatchingRunnable(IForkState forkState, Runnable runnable, CountDownLatch latch, IParamHolder<Throwable> throwableHolder,
			IThreadLocalCleanupController threadLocalCleanupController)
	{
		this.forkState = forkState;
		this.runnable = runnable;
		this.latch = latch;
		this.throwableHolder = throwableHolder;
		this.threadLocalCleanupController = threadLocalCleanupController;
	}

	@Override
	public void run()
	{
		Thread currentThread = Thread.currentThread();
		String oldName = currentThread.getName();
		if (runnable instanceof INamedRunnable)
		{
			currentThread.setName(((INamedRunnable) runnable).getName());
		}
		try
		{
			try
			{
				if (forkState != null)
				{
					forkState.use(runnable);
				}
				else
				{
					runnable.run();
				}
			}
			catch (Throwable e)
			{
				throwableHolder.setValue(e);
			}
			finally
			{
				threadLocalCleanupController.cleanupThreadLocal();
				latch.countDown();
			}
		}
		finally
		{
			if (runnable instanceof INamedRunnable)
			{
				currentThread.setName(oldName);
			}
		}
	}
}
