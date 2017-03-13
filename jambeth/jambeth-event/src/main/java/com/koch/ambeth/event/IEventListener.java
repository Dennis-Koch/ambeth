package com.koch.ambeth.event;

import com.koch.ambeth.ioc.link.LinkOptional;

public interface IEventListener extends IEventListenerMarker
{
	void handleEvent(Object eventObject, @LinkOptional long dispatchTime, @LinkOptional long sequenceId) throws Exception;
}
