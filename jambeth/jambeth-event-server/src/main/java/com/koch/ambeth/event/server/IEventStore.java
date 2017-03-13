package com.koch.ambeth.event.server;

import java.util.List;

public interface IEventStore
{
	void addEvents(List<Object> eventObjects);
}
