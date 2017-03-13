package com.koch.ambeth.event;

public interface IQueuedEvent
{
	Object getEventObject();

	long getDispatchTime();

	void setDispatchTime(long dispatchTime);

	long getSequenceNumber();

	void setSequenceNumber(long sequenceNumber);
}