package de.osthus.ambeth.event.store;

public interface IEventStoreHandler
{
	Object postLoadFromStore(Object eventObject);

	Object preSaveInStore(Object eventObject);

	void eventRemovedFromStore(Object eventObject);
}
