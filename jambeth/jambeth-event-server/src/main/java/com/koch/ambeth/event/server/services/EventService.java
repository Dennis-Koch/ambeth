package com.koch.ambeth.event.server.services;

/*-
 * #%L
 * jambeth-event-server
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

import java.util.List;
import java.util.Random;

import com.koch.ambeth.event.exceptions.EventPollException;
import com.koch.ambeth.event.model.IEventItem;
import com.koch.ambeth.event.server.IEventProvider;
import com.koch.ambeth.event.service.IEventService;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.ParamChecker;

public class EventService implements IEventService, IInitializingBean {
	private static final Random random = new Random();

	protected long serverSession = (long) (random.nextDouble() * Long.MAX_VALUE);

	protected IEventProvider eventProvider;

	@Override
	public void afterPropertiesSet() {
		ParamChecker.assertNotNull(eventProvider, "EventProvider");
	}

	public void setEventProvider(IEventProvider eventProvider) {
		this.eventProvider = eventProvider;
	}

	@Override
	public List<IEventItem> pollEvents(long serverSession, long eventSequenceSince,
			long requestedMaximumWaitTime) {
		if (serverSession != this.serverSession) {
			throw new EventPollException("Client is not in sync with server any more");
		}
		return eventProvider.getEvents(eventSequenceSince, requestedMaximumWaitTime);
	}

	@Override
	public long getCurrentEventSequence() {
		return eventProvider.getCurrentEventSequence();
	}

	@Override
	public long getCurrentServerSession() {
		return serverSession;
	}

	@Override
	public long findEventSequenceNumber(long time) {
		return eventProvider.findEventSequenceNumber(time);
	}
}
