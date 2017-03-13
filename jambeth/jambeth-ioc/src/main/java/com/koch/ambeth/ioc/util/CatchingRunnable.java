package com.koch.ambeth.ioc.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.koch.ambeth.ioc.cancel.ICancellation;
import com.koch.ambeth.ioc.threadlocal.IForkState;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.util.IParamHolder;

public class CatchingRunnable implements RunnableFuture<Throwable>
{
	protected final IForkState forkState;

	protected final Runnable runnable;

	protected final CountDownLatch latch;

	protected final IParamHolder<Throwable> throwableHolder;

	protected final IThreadLocalCleanupController threadLocalCleanupController;

	protected final ICancellation cancellation;

	public CatchingRunnable(IForkState forkState, Runnable runnable, CountDownLatch latch, IParamHolder<Throwable> throwableHolder, ICancellation cancellation,
			IThreadLocalCleanupController threadLocalCleanupController)
	{
		this.forkState = forkState;
		this.runnable = runnable;
		this.latch = latch;
		this.throwableHolder = throwableHolder;
		this.cancellation = cancellation;
		this.threadLocalCleanupController = threadLocalCleanupController;
	}

	@Override
	public Throwable get() throws InterruptedException, ExecutionException
	{
		try
		{
			latch.await();
		}
		catch (InterruptedException e)
		{
			if (cancellation.isCancelled())
			{
				Thread.interrupted(); // clear flag
				cancellation.ensureNotCancelled();
			}
			throw e;
		}
		return throwableHolder.getValue();
	}

	@Override
	public Throwable get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
	{
		try
		{
			if (latch.await(timeout, unit))
			{
				return throwableHolder.getValue();
			}
		}
		catch (InterruptedException e)
		{
			if (cancellation.isCancelled())
			{
				Thread.interrupted(); // clear flag
				cancellation.ensureNotCancelled();
			}
			throw e;
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
					forkState.use(new Runnable()
					{
						@Override
						public void run()
						{
							cancellation.ensureNotCancelled();
							runnable.run();
						}
					});
				}
				else
				{
					cancellation.ensureNotCancelled();
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
