package de.osthus.ambeth.event;

import de.osthus.ambeth.ioc.link.LinkOptional;

public interface IEventListener extends IEventListenerMarker
{
	void handleEvent(Object eventObject, @LinkOptional long dispatchTime, @LinkOptional long sequenceId) throws Exception;
}
