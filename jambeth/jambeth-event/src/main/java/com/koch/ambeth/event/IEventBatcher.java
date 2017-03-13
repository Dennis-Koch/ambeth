package com.koch.ambeth.event;

import java.util.List;

import com.koch.ambeth.util.collections.IList;

public interface IEventBatcher
{
	IList<IQueuedEvent> batchEvents(List<IQueuedEvent> batchableEvents);
}
