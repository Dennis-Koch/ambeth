package com.koch.ambeth.services;

/*-
 * #%L
 * jambeth-test
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

import java.util.ArrayList;
import java.util.List;

import com.koch.ambeth.event.server.IEventStore;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class EventStoreDummy implements IEventStore
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private List<Object> eventObjects = new ArrayList<Object>();

	@Override
	public void addEvents(List<Object> eventObjects)
	{
		this.eventObjects.clear();
		this.eventObjects.addAll(eventObjects);
	}

	public List<Object> getEventObjects()
	{
		return eventObjects;
	}
}
