package com.koch.ambeth.cache.server.datachange.store;

/*-
 * #%L
 * jambeth-cache-server
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

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IQueuedEvent;
import com.koch.ambeth.event.store.IReplacedEvent;
import com.koch.ambeth.merge.objrefstore.ObjRefStore;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IListElem;

public class DataChangeStoreItem extends ArrayList<ObjRefStore>
		implements IQueuedEvent, IListElem<IQueuedEvent>, IReplacedEvent {
	protected final int insertCount, updateCount;

	protected final long changeTime;

	protected long dispatchTime, sequenceNumber;

	protected Object listHandle;

	protected IListElem<IQueuedEvent> prev, next;

	public DataChangeStoreItem(ObjRefStore[] allArray, int insertCount, int updateCount,
			long changeTime) {
		super(allArray);
		this.insertCount = insertCount;
		this.updateCount = updateCount;
		this.changeTime = changeTime;
	}

	@Override
	public Class<?> getOriginalEventType() {
		return IDataChange.class;
	}

	@Override
	public Object getListHandle() {
		return listHandle;
	}

	@Override
	public void setListHandle(Object listHandle) {
		this.listHandle = listHandle;
	}

	@Override
	public IListElem<IQueuedEvent> getPrev() {
		return prev;
	}

	@Override
	public void setPrev(IListElem<IQueuedEvent> prev) {
		this.prev = prev;
	}

	@Override
	public IListElem<IQueuedEvent> getNext() {
		return next;
	}

	@Override
	public void setNext(IListElem<IQueuedEvent> next) {
		this.next = next;
	}

	@Override
	public IQueuedEvent getElemValue() {
		return this;
	}

	@Override
	public void setElemValue(IQueuedEvent value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getEventObject() {
		return this;
	}

	@Override
	public long getDispatchTime() {
		return dispatchTime;
	}

	@Override
	public long getSequenceNumber() {
		return sequenceNumber;
	}

	@Override
	public void setDispatchTime(long dispatchTime) {
		this.dispatchTime = dispatchTime;
	}

	@Override
	public void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
}
