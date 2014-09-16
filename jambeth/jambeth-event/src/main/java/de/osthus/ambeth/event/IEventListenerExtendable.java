package de.osthus.ambeth.event;

public interface IEventListenerExtendable
{
	void registerEventListener(IEventListener eventListener, Class<?> eventType);

	void unregisterEventListener(IEventListener eventListener, Class<?> eventType);

	void registerEventListener(IEventListener eventListener);

	void unregisterEventListener(IEventListener eventListener);
}
