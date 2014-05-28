package de.osthus.ambeth.event;

import java.util.List;

public interface IEventTargetEventListener extends IEventListenerMarker
{
	void handleEvent(Object eventObject, Object resumedEventTarget, List<Object> pausedEventTargets, long dispatchTime, long sequenceId) throws Exception;
}
