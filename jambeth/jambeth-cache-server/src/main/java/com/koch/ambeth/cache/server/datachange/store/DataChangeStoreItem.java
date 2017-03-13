package com.koch.ambeth.cache.server.datachange.store;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IQueuedEvent;
import com.koch.ambeth.event.store.IReplacedEvent;
import com.koch.ambeth.merge.objrefstore.ObjRefStore;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IListElem;

public class DataChangeStoreItem extends ArrayList<ObjRefStore> implements IQueuedEvent, IListElem<IQueuedEvent>, IReplacedEvent
{
	protected final int insertCount, updateCount;

	protected final long changeTime;

	protected long dispatchTime, sequenceNumber;

	protected Object listHandle;

	protected IListElem<IQueuedEvent> prev, next;

	public DataChangeStoreItem(ObjRefStore[] allArray, int insertCount, int updateCount, long changeTime)
	{
		super(allArray);
		this.insertCount = insertCount;
		this.updateCount = updateCount;
		this.changeTime = changeTime;
	}

	@Override
	public Class<?> getOriginalEventType()
	{
		return IDataChange.class;
	}

	@Override
	public Object getListHandle()
	{
		return listHandle;
	}

	@Override
	public void setListHandle(Object listHandle)
	{
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

	@Override
	public Object getEventObject()
	{
		return this;
	}

	@Override
	public long getDispatchTime()
	{
		return dispatchTime;
	}

	@Override
	public long getSequenceNumber()
	{
		return sequenceNumber;
	}

	@Override
	public void setDispatchTime(long dispatchTime)
	{
		this.dispatchTime = dispatchTime;
	}

	@Override
	public void setSequenceNumber(long sequenceNumber)
	{
		this.sequenceNumber = sequenceNumber;
	}
}