package de.osthus.ambeth.event.model;

import de.osthus.ambeth.annotation.XmlType;

@XmlType
public interface IEventItem
{
	Object getEventObject();

	long getSequenceNumber();

	long getDispatchTime();
}
