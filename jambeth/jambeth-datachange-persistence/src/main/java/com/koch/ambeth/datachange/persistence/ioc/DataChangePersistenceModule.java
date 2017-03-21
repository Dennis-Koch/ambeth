package com.koch.ambeth.datachange.persistence.ioc;

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
import com.koch.ambeth.datachange.persistence.services.DataChangeEventDAO;
import com.koch.ambeth.datachange.persistence.services.DataChangeEventService;
import com.koch.ambeth.datachange.persistence.services.DataChangePersistenceListener;
import com.koch.ambeth.datachange.persistence.services.IDataChangeEventDAO;
import com.koch.ambeth.datachange.persistence.services.IDataChangeEventService;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class DataChangePersistenceModule implements IInitializingModule {
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean("dataChangeEventDAO", DataChangeEventDAO.class)
				.autowireable(IDataChangeEventDAO.class);

		beanContextFactory.registerBean("dataChangeEventService", DataChangeEventService.class)
				.autowireable(IDataChangeEventService.class);

		beanContextFactory.registerBean("dataChangePersistenceListener",
				DataChangePersistenceListener.class);
		beanContextFactory.link("dataChangePersistenceListener").to(IEventListenerExtendable.class)
				.with(IDataChange.class);
	}
}
