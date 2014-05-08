package de.osthus.ambeth.event;

public interface IEventListener extends IEventListenerMarker
{
	void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception;
}
