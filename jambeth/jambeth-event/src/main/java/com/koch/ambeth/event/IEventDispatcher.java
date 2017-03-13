package com.koch.ambeth.event;

import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public interface IEventDispatcher extends IEventQueue
{
	void dispatchEvent(Object eventObject);

	void dispatchEvent(Object eventObject, long dispatchTime, long sequenceId);

	/**
	 * Checks for listeners for a given event type. This method should be used before constructing expensive events objects. Do not bother if you already have
	 * all data for your event and just have to create the event object.
	 * 
	 * @param eventType
	 *            Type to check for listeners
	 * @return <code>true</code> iff there are listeners for the event type, <code>false</code> otherwise
	 */
	boolean hasListeners(Class<?> eventType);

	void waitEventToResume(Object eventTargetToResume, long maxWaitTime, IBackgroundWorkerParamDelegate<IProcessResumeItem> resumeDelegate,
			IBackgroundWorkerParamDelegate<Throwable> errorDelegate);
}
