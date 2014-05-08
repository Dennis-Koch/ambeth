package de.osthus.ambeth.event;

import java.util.List;

import de.osthus.ambeth.collections.IList;

public interface IEventBatcher
{
	IList<IQueuedEvent> batchEvents(List<IQueuedEvent> batchableEvents);
}
