package com.koch.ambeth.event;

public interface IEventTargetExtractorExtendable
{
	void registerEventTargetExtractor(IEventTargetExtractor eventTargetExtractor, Class<?> eventType);

	void unregisterEventTargetExtractor(IEventTargetExtractor eventTargetExtractor, Class<?> eventType);
}
