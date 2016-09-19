package de.osthus.ambeth.event;

import java.util.List;

/**
 * A more complex version of the {@link IEventListener}. Here the implementor gets additional access to eventTargets which are needed for more complex
 * synchronization scenarios. One of its usecases is in the {@link de.osthus.ambeth.cache.CacheDataChangeListener} where all corresponding cache instances have
 * to be "reserved" before the DataChangeEvent should be processed.<br/>
 * <br/>
 * 
 * The concept of "eventTargets" here connects with the {@link IEventQueue#pause(Object)} and {@link IEventQueue#resume(Object)} methods. In the mentioned
 * example above all Cache instances use these methods to propagate that they are "under use" by application code when necessary.
 */
public interface IEventTargetEventListener extends IEventListenerMarker
{
	void handleEvent(Object eventObject, Object resumedEventTarget, List<Object> pausedEventTargets, long dispatchTime, long sequenceId) throws Exception;
}
