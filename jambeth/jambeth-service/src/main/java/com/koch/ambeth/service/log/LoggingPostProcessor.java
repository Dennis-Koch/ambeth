package com.koch.ambeth.service.log;

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

import java.util.Set;

import com.koch.ambeth.ioc.IOrderedBeanProcessor;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.ProcessorOrder;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.log.interceptor.LogInterceptor;
import com.koch.ambeth.service.proxy.AbstractCascadePostProcessor;
import com.koch.ambeth.util.proxy.Callback;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;

public class LoggingPostProcessor extends AbstractCascadePostProcessor
		implements IOrderedBeanProcessor {
	@Property(name = ServiceConfigurationConstants.WrapAllInteractions, defaultValue = "false")
	protected boolean wrapAllInteractions;

	@Property(name = ServiceConfigurationConstants.LogShortNames, defaultValue = "false")
	protected boolean printShortStringNames;

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory,
			IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> type,
			Set<Class<?>> requestedTypes) {
		if (!type.isAnnotationPresent(LogException.class)
				&& (!wrapAllInteractions || Callback.class.isAssignableFrom(type))) {
			return null;
		}
		var logInterceptor = new LogInterceptor();
		if (beanContext.isRunning()) {
			return beanContext.registerWithLifecycle(logInterceptor).finish();
		}
		beanContextFactory.registerWithLifecycle(logInterceptor);
		return logInterceptor;
	}

	@Override
	public ProcessorOrder getOrder() {
		return ProcessorOrder.HIGHEST;
	}
}
