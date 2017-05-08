package com.koch.ambeth.service;

/*-
 * #%L
 * jambeth-service
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;

public class ServiceByNameProvider
		implements IServiceByNameProvider, IServiceExtendable, IInitializingBean {
	@SuppressWarnings("unused")
	@LogInstance(ServiceByNameProvider.class)
	private ILogger log;

	protected MapExtendableContainer<String, Object> serviceNameToObjectMap;

	protected IServiceByNameProvider parentServiceByNameProvider;

	@Override
	public void afterPropertiesSet() throws Throwable {
		if (parentServiceByNameProvider != null) {
			ParamChecker.assertTrue(parentServiceByNameProvider != this, "parentServiceByNameProvider");
		}

		serviceNameToObjectMap = new MapExtendableContainer<>("serviceName", "service");
	}

	public void setParentServiceByNameProvider(IServiceByNameProvider parentServiceByNameProvider) {
		this.parentServiceByNameProvider = parentServiceByNameProvider;
	}

	@Override
	public void registerService(Object service, String serviceName) {
		serviceNameToObjectMap.register(service, serviceName);
	}

	@Override
	public void unregisterService(Object service, String serviceName) {
		serviceNameToObjectMap.unregister(service, serviceName);
	}

	@Override
	public Object getService(String serviceName) {
		Object service = serviceNameToObjectMap.getExtension(serviceName);
		if (service == null && parentServiceByNameProvider != null) {
			service = parentServiceByNameProvider.getService(serviceName);
		}
		return service;
	}
}
