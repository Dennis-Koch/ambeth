package de.osthus.ambeth.services;

import java.util.List;
import java.util.Random;

import de.osthus.ambeth.event.IEventProvider;
import de.osthus.ambeth.event.model.IEventItem;
import de.osthus.ambeth.exceptions.EventPollException;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.service.IEventService;
import de.osthus.ambeth.util.ParamChecker;

public class EventService implements IEventService, IInitializingBean
{
	private static final Random random = new Random();

	protected long serverSession = (long) (random.nextDouble() * Long.MAX_VALUE);

	protected IEventProvider eventProvider;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(eventProvider, "EventProvider");
	}

	public void setEventProvider(IEventProvider eventProvider)
	{
		this.eventProvider = eventProvider;
	}

	@Override
	public List<IEventItem> pollEvents(long serverSession, long eventSequenceSince, long requestedMaximumWaitTime)
	{
		if (serverSession != this.serverSession)
		{
			throw new EventPollException("Client is not in sync with server any more");
		}
		return eventProvider.getEvents(eventSequenceSince, requestedMaximumWaitTime);
	}

	@Override
	public long getCurrentEventSequence()
	{
		return eventProvider.getCurrentEventSequence();
	}

	@Override
	public long getCurrentServerSession()
	{
		return serverSession;
	}

	@Override
	public long findEventSequenceNumber(long time)
	{
		return eventProvider.findEventSequenceNumber(time);
	}
}
