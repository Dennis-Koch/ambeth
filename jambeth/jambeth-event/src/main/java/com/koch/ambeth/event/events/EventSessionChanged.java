package com.koch.ambeth.event.events;

import com.koch.ambeth.event.service.IEventService;

public class EventSessionChanged {
	private final IEventService eventService;

	private final long oldSession, newSession;

	public EventSessionChanged(IEventService eventService, long oldSession, long newSession) {
		super();
		this.eventService = eventService;
		this.oldSession = oldSession;
		this.newSession = newSession;
	}

	public IEventService getEventService() {
		return eventService;
	}

	public long getOldSession() {
		return oldSession;
	}

	public long getNewSession() {
		return newSession;
	}
}
