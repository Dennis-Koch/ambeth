package com.koch.ambeth.ioc.cancel;

import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;

public class Cancellation implements ICancellation, ICancellationWritable, IThreadLocalCleanupBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Forkable
	protected final ThreadLocal<ICancellationHandle> cancelledTL = new ThreadLocal<ICancellationHandle>();

	@Override
	public void cleanupThreadLocal()
	{
		cancelledTL.set(null);
	}

	@Override
	public boolean isCancelled()
	{
		ICancellationHandle cancellationHandle = cancelledTL.get();
		if (cancellationHandle == null)
		{
			return false;
		}
		return cancellationHandle.isCancelled();
	}

	@Override
	public void withCancellationAwareness(IBackgroundWorkerDelegate runnable)
	{
		ensureNotCancelled();
		ICancellationHandle cancellationHandle = cancelledTL.get();
		if (cancellationHandle == null)
		{
			try
			{
				runnable.invoke();
				return;
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		cancellationHandle.withCancellationAwareness(runnable);
	}

	@Override
	public <R> R withCancellationAwareness(IResultingBackgroundWorkerDelegate<R> runnable)
	{
		ensureNotCancelled();
		ICancellationHandle cancellationHandle = cancelledTL.get();
		if (cancellationHandle == null)
		{
			try
			{
				return runnable.invoke();
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return cancellationHandle.withCancellationAwareness(runnable);
	}

	@Override
	public <R, V> R withCancellationAwareness(IResultingBackgroundWorkerParamDelegate<R, V> runnable, V state)
	{
		ensureNotCancelled();
		ICancellationHandle cancellationHandle = cancelledTL.get();
		if (cancellationHandle == null)
		{
			try
			{
				return runnable.invoke(state);
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return cancellationHandle.withCancellationAwareness(runnable, state);
	}

	@Override
	public void ensureNotCancelled()
	{
		if (isCancelled())
		{
			throw new CancelledException();
		}
	}

	@Override
	public ICancellationHandle getEnsureCancellationHandle()
	{
		ensureNotCancelled();
		ICancellationHandle cancellationHandle = cancelledTL.get();
		if (cancellationHandle == null)
		{
			cancellationHandle = createUnassignedCancellationHandle();
			cancelledTL.set(cancellationHandle);
		}
		return cancellationHandle;
	}

	@Override
	public ICancellationHandle createUnassignedCancellationHandle()
	{
		return new CancellationHandle(this);
	}

	@Override
	public IStateRollback pushCancellationHandle(final ICancellationHandle cancellationHandle)
	{
		ParamChecker.assertParamNotNull(cancellationHandle, "cancellationHandle");
		final boolean hasBeenAdded = ((CancellationHandle) cancellationHandle).addOwningThread();
		final ICancellationHandle oldCancellationHandle = cancelledTL.get();
		cancelledTL.set(cancellationHandle);
		return new IStateRollback()
		{
			@Override
			public void rollback()
			{
				if (hasBeenAdded)
				{
					((CancellationHandle) cancellationHandle).removeOwningThread();
				}
				cancelledTL.set(oldCancellationHandle);
			}
		};
	}
}
