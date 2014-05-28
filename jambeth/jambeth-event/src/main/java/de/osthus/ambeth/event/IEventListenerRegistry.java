package de.osthus.ambeth.event;

public interface IEventListenerRegistry
{
	void registerEventListener(IEventListener eventListener, Class<?>... eventTypes);

	void unregisterEventListener(IEventListener eventListener, Class<?>... eventTypes);
}
