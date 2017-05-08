package com.koch.ambeth.datachange.kafka;

/*-
 * #%L
 * jambeth-datachange-kafka
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
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class ForwardLocalDataChangeToKafka implements IEventListener {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEventListener eventListener;

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception {
		if (!(eventObject instanceof IDataChange)) {
			return;
		}
		IDataChange dataChange = (IDataChange) eventObject;
		if (dataChange.isEmpty() || !dataChange.isLocalSource()) {
			return;
		}
		// ONLY forward events where localSource=true so the condition above is important
		// otherwise we would "bounce" a foreign event back to kafka in an endless loop
		eventListener.handleEvent(eventObject, dispatchTime, sequenceId);
	}
}
