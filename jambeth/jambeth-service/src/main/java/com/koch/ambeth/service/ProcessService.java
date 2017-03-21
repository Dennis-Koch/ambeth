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

import java.lang.reflect.Method;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class ProcessService implements IProcessService
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IServiceByNameProvider serviceByNameProvider;

	@Override
	public Object invokeService(IServiceDescription serviceDescription)
	{
		ParamChecker.assertParamNotNull(serviceDescription, "serviceDescription");

		Object service = serviceByNameProvider.getService(serviceDescription.getServiceName());

		if (service == null)
		{
			throw new IllegalStateException("No service with name '" + serviceDescription.getServiceName() + "' found");
		}
		Method method = serviceDescription.getMethod(service.getClass(), objectCollector);
		if (method == null)
		{
			throw new IllegalStateException("Requested method not found on service '" + serviceDescription.getServiceName() + "'");
		}
		try
		{
			return method.invoke(service, serviceDescription.getArguments());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
