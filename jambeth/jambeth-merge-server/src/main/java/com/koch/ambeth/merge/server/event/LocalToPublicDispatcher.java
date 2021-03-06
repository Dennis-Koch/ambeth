package com.koch.ambeth.merge.server.event;

/*-
 * #%L
 * jambeth-merge-server
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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.model.IDataChangeOfSession;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.persistence.event.DatabaseAcquireEvent;
import com.koch.ambeth.persistence.event.DatabaseCommitEvent;
import com.koch.ambeth.persistence.event.DatabaseFailEvent;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IList;

public class LocalToPublicDispatcher implements IEventListener {
	@Autowired
	protected IEventDispatcher publicEventDispatcher;

	protected final HashMap<Long, ArrayList<IDataChange>> databaseToChangeDict =
			new HashMap<>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void handleEvent(Object localEventObject, long dispatchTime, long sequenceId) {
		if (localEventObject instanceof DatabaseAcquireEvent) {
			DatabaseAcquireEvent localEvent = (DatabaseAcquireEvent) localEventObject;
			Lock writeLock = this.writeLock;
			writeLock.lock();
			try {
				databaseToChangeDict.put(localEvent.getSessionId(), null);
			}
			finally {
				writeLock.unlock();
			}
		}
		else if (localEventObject instanceof DatabaseCommitEvent) {
			DatabaseCommitEvent localEvent = (DatabaseCommitEvent) localEventObject;
			IList<IDataChange> publicDataChanges;
			Lock writeLock = this.writeLock;
			writeLock.lock();
			try {
				publicDataChanges = databaseToChangeDict.remove(localEvent.getSessionId());
			}
			finally {
				writeLock.unlock();
			}
			if (publicDataChanges != null) {
				IEventDispatcher publicEventDispatcher = this.publicEventDispatcher;
				if (publicDataChanges.size() > 1) {
					publicEventDispatcher.enableEventQueue();
				}
				try {
					for (int i = 0; i < publicDataChanges.size(); i++) {
						IDataChange publicDataChange = publicDataChanges.get(i);
						publicEventDispatcher.dispatchEvent(publicDataChange, dispatchTime, sequenceId);
					}
				}
				finally {
					if (publicDataChanges.size() > 1) {
						publicEventDispatcher.flushEventQueue();
					}
				}
			}
		}
		else if (localEventObject instanceof DatabaseFailEvent) {
			DatabaseFailEvent localEvent = (DatabaseFailEvent) localEventObject;
			Lock writeLock = this.writeLock;
			writeLock.lock();
			try {
				databaseToChangeDict.remove(localEvent.getSessionId());
			}
			finally {
				writeLock.unlock();
			}
		}
		else if (localEventObject instanceof IDataChangeOfSession) {
			IDataChangeOfSession localEvent = (IDataChangeOfSession) localEventObject;
			Lock writeLock = this.writeLock;
			writeLock.lock();
			try {
				ArrayList<IDataChange> dataChanges = databaseToChangeDict.get(localEvent.getSessionId());
				if (dataChanges == null) {
					dataChanges = new ArrayList<>();
					databaseToChangeDict.put(localEvent.getSessionId(), dataChanges);
				}
				dataChanges.add(localEvent.getDataChange());
			}
			finally {
				writeLock.unlock();
			}
		}
	}
}
