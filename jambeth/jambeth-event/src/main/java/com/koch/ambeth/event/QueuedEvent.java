package com.koch.ambeth.event;

/*-
 * #%L
 * jambeth-event
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.util.collections.IListElem;

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
