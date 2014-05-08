package de.osthus.ambeth.cache;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;

public class CacheModification implements ICacheModification
{
	private static final Integer ACTIVE = Integer.valueOf(1), FLUSHING = Integer.valueOf(2);

	@LogInstance
	private ILogger log;

	// Intentionally no SensitiveThreadLocal
	protected final ThreadLocal<Integer> activeTL = new ThreadLocal<Integer>();

	protected final ThreadLocal<ArrayList<IBackgroundWorkerDelegate>> queuedEventsTL = new ThreadLocal<ArrayList<IBackgroundWorkerDelegate>>();

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
