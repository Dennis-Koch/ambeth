package de.osthus.ambeth.cancel;

import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.state.IStateRollback;
import de.osthus.ambeth.util.ParamChecker;

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
			((CancellationHandle) cancellationHandle).addOwningThread();
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
	public IStateRollback setCancellationHandle(ICancellationHandle cancellationHandle)
	{
		ParamChecker.assertParamNotNull(cancellationHandle, "cancellationHandle");
		((CancellationHandle) cancellationHandle).addOwningThread();
		final ICancellationHandle oldCancellationHandle = cancelledTL.get();
		cancelledTL.set(cancellationHandle);
		return new IStateRollback()
		{
			@Override
			public void rollback()
			{
				cancelledTL.set(oldCancellationHandle);
			}
		};
	}
}
