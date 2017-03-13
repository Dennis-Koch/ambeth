package com.koch.ambeth.event;

public interface IEventBatcherExtendable
{
	void registerEventBatcher(IEventBatcher eventBatcher, Class<?> eventType);

	void unregisterEventBatcher(IEventBatcher eventBatcher, Class<?> eventType);
}
