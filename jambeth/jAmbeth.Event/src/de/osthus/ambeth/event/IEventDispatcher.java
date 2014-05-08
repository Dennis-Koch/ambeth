package de.osthus.ambeth.event;

import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;

public interface IEventDispatcher extends IEventQueue
{
	void dispatchEvent(Object eventObject);

	void dispatchEvent(Object eventObject, long dispatchTime, long sequenceId);

	void waitEventToResume(Object eventTargetToResume, long maxWaitTime, IBackgroundWorkerParamDelegate<IProcessResumeItem> resumeDelegate,
			IBackgroundWorkerParamDelegate<Throwable> errorDelegate);
}
