package de.osthus.ambeth.event.store;

public interface IEventStoreHandlerExtendable
{
	void registerEventStoreHandler(IEventStoreHandler eventStoreHandler, Class<?> eventType);

	void unregisterEventStoreHandler(IEventStoreHandler eventStoreHandler, Class<?> eventType);
}
