package com.koch.ambeth.event.service;

import java.util.List;

import com.koch.ambeth.event.model.IEventItem;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IEventService
{
	List<IEventItem> pollEvents(long serverSession, long eventSequenceSince, long requestedMaximumWaitTime);

	long getCurrentEventSequence();

	long getCurrentServerSession();

	long findEventSequenceNumber(long time);
}
