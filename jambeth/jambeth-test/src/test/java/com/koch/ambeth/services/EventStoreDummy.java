package com.koch.ambeth.services;

import java.util.ArrayList;
import java.util.List;

import com.koch.ambeth.event.server.IEventStore;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

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
