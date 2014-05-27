package de.osthus.ambeth.event;

import de.osthus.ambeth.collections.IListElem;

public class QueuedEvent implements IQueuedEvent, IListElem<IQueuedEvent>
{
	protected final Object eventObject;

	protected long dispatchTime;

	protected long sequenceNumber;

	protected Object listHandle;

	protected IListElem<IQueuedEvent> next, prev;

	public QueuedEvent(Object eventObject, long dispatchTime, long sequenceNumber)
	{
		this.eventObject = eventObject;
		this.dispatchTime = dispatchTime;
		this.sequenceNumber = sequenceNumber;
	}

	@Override
	public Object getEventObject()
	{
		return eventObject;
	}

	@Override
	public long getDispatchTime()
	{
		return dispatchTime;
	}

	@Override
	public void setDispatchTime(long dispatchTime)
	{
		this.dispatchTime = dispatchTime;
	}

	@Override
	public long getSequenceNumber()
	{
		return sequenceNumber;
	}

	@Override
	public void setSequenceNumber(long sequenceNumber)
	{
		this.sequenceNumber = sequenceNumber;
	}

	@Override
	public Object getListHandle()
	{
		return listHandle;
	}

	@Override
	public void setListHandle(Object listHandle)
	{
		if (this.listHandle != null && listHandle != null)
		{
			throw new UnsupportedOperationException();
		}
		this.listHandle = listHandle;
	}

	@Override
	public IListElem<IQueuedEvent> getPrev()
	{
		return prev;
	}

	@Override
	public void setPrev(IListElem<IQueuedEvent> prev)
	{
		this.prev = prev;
	}

	@Override
	public IListElem<IQueuedEvent> getNext()
	{
		return next;
	}

	@Override
	public void setNext(IListElem<IQueuedEvent> next)
	{
		this.next = next;
	}

	@Override
	public IQueuedEvent getElemValue()
	{
		return this;
	}

	@Override
	public void setElemValue(IQueuedEvent value)
	{
		throw new UnsupportedOperationException();
	}
}