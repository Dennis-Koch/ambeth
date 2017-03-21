package com.koch.ambeth.event.datachange.ioc;

/*-
 * #%L
 * jambeth-event-datachange
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
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.event.config.EventConfigurationConstants;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

@FrameworkModule
public class EventDataChangeModule implements IInitializingModule
{
	@Property(name = EventConfigurationConstants.EventManagerName, mandatory = false)
	protected String eventManagerName;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (eventManagerName != null)
		{
			beanContextFactory.link(eventManagerName).to(IEventListenerExtendable.class).with(IDataChange.class);
		}
	}
}
