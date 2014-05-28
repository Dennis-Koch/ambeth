package de.osthus.ambeth.event;

import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public interface IEventQueue
{
	void enableEventQueue();

	void flushEventQueue();

	void pause(Object eventTarget);

	void resume(Object eventTarget);

	<R> R invokeWithoutLocks(IResultingBackgroundWorkerDelegate<R> runnable);
}
