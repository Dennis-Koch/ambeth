package de.osthus.ambeth.event;


public interface IEventQueue
{
	void enableEventQueue();

	void flushEventQueue();

	void pause(Object eventTarget);

	void resume(Object eventTarget);
}
