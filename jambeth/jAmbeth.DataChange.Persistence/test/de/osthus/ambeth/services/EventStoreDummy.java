package de.osthus.ambeth.services;

import java.util.ArrayList;
import java.util.List;

import de.osthus.ambeth.event.IEventStore;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class EventStoreDummy implements IEventStore
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private List<Object> eventObjects = new ArrayList<Object>();

	@Override
	public void addEvents(List<Object> eventObjects)
	{
		this.eventObjects.clear();
		this.eventObjects.addAll(eventObjects);
	}

	public List<Object> getEventObjects()
	{
		return eventObjects;
	}
}
