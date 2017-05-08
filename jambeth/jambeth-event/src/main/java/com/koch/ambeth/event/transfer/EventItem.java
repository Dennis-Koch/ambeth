package com.koch.ambeth.event.transfer;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.event.model.IEventItem;

@XmlRootElement(name = "EventItem", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventItem implements IEventItem {
	@XmlElement(required = true)
	protected Object eventObject;

	@XmlElement(required = true)
	protected long sequenceNumber;

	@XmlElement(required = true)
	protected long dispatchTime;

	public EventItem() {
		// Intended blank
	}

	public EventItem(Object eventObject, long dispatchTime, long sequenceNumber) {
		this.eventObject = eventObject;
		this.dispatchTime = dispatchTime;
		this.sequenceNumber = sequenceNumber;
	}

	@Override
	public Object getEventObject() {
		return eventObject;
	}

	public void setEventObject(Object EO) {
		eventObject = EO;
	}

	@Override
	public long getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	@Override
	public long getDispatchTime() {
		return dispatchTime;
	}

	public void setDispatchTime(long dispatchTime) {
		this.dispatchTime = dispatchTime;
	}
}
