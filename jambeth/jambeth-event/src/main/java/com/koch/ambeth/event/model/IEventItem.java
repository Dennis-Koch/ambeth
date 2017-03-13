package com.koch.ambeth.event.model;

import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IEventItem
{
	Object getEventObject();

	long getSequenceNumber();

	long getDispatchTime();
}
