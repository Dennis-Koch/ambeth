package de.osthus.ambeth.event;

import java.util.List;

public interface IEventStore
{
	void addEvents(List<Object> eventObjects);
}
