package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.event.model.IEventItem;

@XmlType
public interface IEventService
{
	List<IEventItem> pollEvents(long serverSession, long eventSequenceSince, long requestedMaximumWaitTime);

	long getCurrentEventSequence();

	long getCurrentServerSession();

	long findEventSequenceNumber(long time);
}
