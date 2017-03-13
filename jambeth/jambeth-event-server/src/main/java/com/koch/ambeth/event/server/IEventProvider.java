package com.koch.ambeth.event.server;

import java.util.List;

import com.koch.ambeth.event.model.IEventItem;

public interface IEventProvider
{
	List<IEventItem> getEvents(long eventSequenceSince, long requestedMaximumWaitTime);

	long getCurrentEventSequence();

	long findEventSequenceNumber(long time);
}
