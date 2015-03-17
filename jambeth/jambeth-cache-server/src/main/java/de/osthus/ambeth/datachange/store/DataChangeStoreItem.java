package de.osthus.ambeth.datachange.store;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IListElem;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.event.IQueuedEvent;
import de.osthus.ambeth.event.store.IReplacedEvent;
import de.osthus.ambeth.objrefstore.ObjRefStore;

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