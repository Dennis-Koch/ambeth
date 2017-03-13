package com.koch.ambeth.event;

public interface IEventTargetListenerExtendable
{
	void registerEventTargetListener(IEventTargetEventListener eventTargetListener, Class<?> eventType);

	void unregisterEventTargetListener(IEventTargetEventListener eventTargetListener, Class<?> eventType);

	void registerEventTargetListener(IEventTargetEventListener eventTargetListener);

	void unregisterEventTargetListener(IEventTargetEventListener eventTargetListener);
}
