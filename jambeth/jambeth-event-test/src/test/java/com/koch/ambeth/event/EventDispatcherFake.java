package com.koch.ambeth.event;

import java.util.ArrayList;
import java.util.List;

import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.event.IProcessResumeItem;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public class EventDispatcherFake implements IEventDispatcher
{
	public final List<DispatchedEvent> dispatchedEvents = new ArrayList<DispatchedEvent>();

	class DispatchedEvent
	{
		public Object eventObject;
		public long dispatchTime;
		public long sequenceId;

		public DispatchedEvent(Object eventObject, long dispatchTime, long sequenceId)
		{
			this.eventObject = eventObject;
			this.dispatchTime = dispatchTime;
			this.sequenceId = sequenceId;
		}
	}

	@Override
	public boolean isDispatchingBatchedEvents()
	{
		return false;
	}

	@Override
	public void dispatchEvent(Object eventObject)
	{
		dispatchEvent(eventObject, System.currentTimeMillis(), -1);
	}

	@Override
	public void dispatchEvent(Object eventObject, long dispatchTime, long sequenceId)
	{
		dispatchedEvents.add(new DispatchedEvent(eventObject, dispatchTime, sequenceId));
	}

	@Override
	public boolean hasListeners(Class<?> eventType)
	{
		return true;
	}

	@Override
	public void waitEventToResume(Object eventTargetToResume, long maxWaitTime, IBackgroundWorkerParamDelegate<IProcessResumeItem> resumeDelegate,
			IBackgroundWorkerParamDelegate<Throwable> errorDelegate)
	{
		try
		{
			resumeDelegate.invoke(null);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void enableEventQueue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void flushEventQueue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void pause(Object eventTarget)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void resume(Object eventTarget)
	{
		throw new UnsupportedOperationException();
	}
}
