package de.osthus.ambeth.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.osthus.ambeth.ioc.threadlocal.IForkState;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;

public class CatchingRunnable implements RunnableFuture<Throwable>
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
	public Throwable get() throws InterruptedException, ExecutionException
	{
		latch.await();
		return throwableHolder.getValue();
	}

	@Override
	public Throwable get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
	{
		if (latch.await(timeout, unit))
		{
			return throwableHolder.getValue();
		}
		throw new TimeoutException("No result after " + timeout + " " + unit.name());
	}

	@Override
	public boolean isDone()
	{
		return latch.getCount() == 0;
	}

	@Override
	public boolean isCancelled()
	{
		return false;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning)
	{
		return false;
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
