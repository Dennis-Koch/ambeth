package com.koch.ambeth.datachange.persistence.services;

/*-
 * #%L
 * jambeth-datachange-persistence
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
import com.koch.ambeth.datachange.persistence.model.DataChangeEntryBO;
import com.koch.ambeth.datachange.persistence.model.DataChangeEventBO;
import com.koch.ambeth.datachange.persistence.model.EntityType;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;

public class DataChangePersistenceListener implements IEventListener, IInitializingBean {
	private static final Class<?>[] uninterestingTypes =
			{DataChangeEventBO.class, DataChangeEntryBO.class, EntityType.class};

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IDataChangeEventService dataChangeEventService;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(dataChangeEventService, "dataChangeEventService");
	}

	public void setDataChangeEventService(IDataChangeEventService dataChangeEventService) {
		this.dataChangeEventService = dataChangeEventService;
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) {
		if (!(eventObject instanceof IDataChange)) {
			return;
		}

		IDataChange dataChange = (IDataChange) eventObject;
		dataChange = dataChange.deriveNot(uninterestingTypes);
		if (dataChange.getAll().isEmpty()) {
			return;
		}

		dataChangeEventService.save(dataChange);
	}
}
