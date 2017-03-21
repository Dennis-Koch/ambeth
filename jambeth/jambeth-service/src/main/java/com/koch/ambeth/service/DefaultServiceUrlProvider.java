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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class DefaultServiceUrlProvider implements IServiceUrlProvider, IOfflineListenerExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Property(name = ServiceConfigurationConstants.ServiceBaseUrl, defaultValue = "http://localhost:8000")
	protected String serviceBaseUrl;

	@Override
	public String getServiceURL(Class<?> serviceInterface, String serviceName)
	{
		return StringBuilderUtil.concat(objectCollector.getCurrent(), serviceBaseUrl, "/", serviceName, "/");
	}

	@Override
	public boolean isOffline()
	{
		return false;
	}

	@Override
	public void setOffline(boolean isOffline)
	{
		throw new UnsupportedOperationException("This " + IServiceUrlProvider.class.getSimpleName() + " does not support this operation");
	}

	@Override
	public void lockForRestart(boolean offlineAfterRestart)
	{
		throw new UnsupportedOperationException("This " + IServiceUrlProvider.class.getSimpleName() + " does not support this operation");
	}

	@Override
	public void addOfflineListener(IOfflineListener offlineListener)
	{
		// Intended NoOp!
	}

	@Override
	public void removeOfflineListener(IOfflineListener offlineListener)
	{
		// Intended NoOp!
	}
}
