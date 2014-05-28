package de.osthus.ambeth.event.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.event.model.IEventItem;

@XmlRootElement(name = "EventItem", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventItem implements IEventItem
{
	@XmlElement(required = true)
	protected Object eventObject;

	@XmlElement(required = true)
	protected long sequenceNumber;

	@XmlElement(required = true)
	protected long dispatchTime;

	public EventItem()
	{
		// Intended blank
	}

	public EventItem(Object eventObject, long dispatchTime, long sequenceNumber)
	{
		this.eventObject = eventObject;
		this.dispatchTime = dispatchTime;
		this.sequenceNumber = sequenceNumber;
	}

	@Override
	public Object getEventObject()
	{
		return this.eventObject;
	}

	public void setEventObject(Object EO)
	{
		this.eventObject = EO;
	}

	@Override
	public long getSequenceNumber()
	{
		return this.sequenceNumber;
	}

	public void setSequenceNumber(long sequenceNumber)
	{
		this.sequenceNumber = sequenceNumber;
	}

	@Override
	public long getDispatchTime()
	{
		return this.dispatchTime;
	}

	public void setDispatchTime(long dispatchTime)
	{
		this.dispatchTime = dispatchTime;
	}
}
