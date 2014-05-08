package de.osthus.ambeth.event;

public interface IEventListenerExtendable
{
	void registerEventListener(IEventListenerMarker eventListener, Class<?> eventType);

	void unregisterEventListener(IEventListenerMarker eventListener, Class<?> eventType);

	void registerEventListener(IEventListenerMarker eventListener);

	void unregisterEventListener(IEventListenerMarker eventListener);
}
