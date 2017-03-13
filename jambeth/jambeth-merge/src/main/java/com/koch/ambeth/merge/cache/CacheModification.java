package com.koch.ambeth.merge.cache;

import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;

public class CacheModification implements ICacheModification, IThreadLocalCleanupBean
{
	private static final Integer ACTIVE = Integer.valueOf(1), FLUSHING = Integer.valueOf(2);

	@LogInstance
	private ILogger log;

	// Intentionally no SensitiveThreadLocal
	@Forkable
	protected final ThreadLocal<Integer> activeTL = new ThreadLocal<Integer>();

	@Forkable
	protected final ThreadLocal<Boolean> internalUpdateTL = new ThreadLocal<Boolean>();

	protected final ThreadLocal<ArrayList<IBackgroundWorkerDelegate>> queuedEventsTL = new ThreadLocal<ArrayList<IBackgroundWorkerDelegate>>();

	@Override
	public void cleanupThreadLocal()
	{
		activeTL.remove();
		internalUpdateTL.remove();
		queuedEventsTL.remove();
	}

	@Override
	public boolean isActiveOrFlushing()
	{
		return activeTL.get() != null;
	}

	@Override
	public boolean isActive()
	{
		return ACTIVE.equals(activeTL.get());
	}

	@Override
	public boolean isInternalUpdate()
	{
		Boolean internalUpdate = internalUpdateTL.get();
		return internalUpdate != null ? internalUpdate.booleanValue() : false;
	}

	@Override
	public boolean isActiveOrFlushingOrInternalUpdate()
	{
		return isActiveOrFlushing() || isInternalUpdate();
	}

	@Override
	public void setInternalUpdate(boolean internalUpdate)
	{
		if (internalUpdate)
		{
			internalUpdateTL.set(Boolean.TRUE);
		}
		else
		{
			internalUpdateTL.remove();
		}
	}

	@Override
	public void setActive(boolean active)
	{
		boolean existingIsActive = isActive();
		if (existingIsActive == active)
		{
			return;
		}
		if (existingIsActive)
		{
			activeTL.set(FLUSHING);
			try
			{
				fireQueuedPropertyChangeEvents();
			}
			finally
			{
				activeTL.remove();
			}
		}
		else
		{
			activeTL.set(ACTIVE);
		}
	}

	@Override
	public void queuePropertyChangeEvent(IBackgroundWorkerDelegate task)
	{
		if (!isActive())
		{
			throw new IllegalStateException("Not supported if isActive() is 'false'");
		}
		ArrayList<IBackgroundWorkerDelegate> queuedEvents = queuedEventsTL.get();
		if (queuedEvents == null)
		{
			queuedEvents = new ArrayList<IBackgroundWorkerDelegate>();
			queuedEventsTL.set(queuedEvents);
		}
		queuedEvents.add(task);
	}

	protected void fireQueuedPropertyChangeEvents()
	{
		ArrayList<IBackgroundWorkerDelegate> queuedEvents = queuedEventsTL.get();
		if (queuedEvents == null)
		{
			return;
		}
		queuedEventsTL.remove();
		try
		{
			for (int a = 0, size = queuedEvents.size(); a < size; a++)
			{
				IBackgroundWorkerDelegate queuedEvent = queuedEvents.get(a);
				queuedEvent.invoke();
			}
		}
		catch (Throwable e)
		{
			log.error(e);
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
