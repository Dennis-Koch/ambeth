package de.osthus.ambeth.event;

import java.util.List;

import de.osthus.ambeth.event.model.IEventItem;

public interface IEventProvider
{
	List<IEventItem> getEvents(long eventSequenceSince, long requestedMaximumWaitTime);

	long getCurrentEventSequence();

	long findEventSequenceNumber(long time);
}
