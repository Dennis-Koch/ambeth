package com.koch.ambeth.ioc.postprocessor;

/*-
 * #%L
 * jambeth-ioc-test
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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.ProcessorOrder;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class BeanPostProcessorTestModule implements IInitializingModule {
	public static final String NumberOfPostProcessors = "numberOfPostProcessors";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = NumberOfPostProcessors)
	protected int number;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		ProcessorOrder[] orders = ProcessorOrder.values();
		for (int a = number; a-- > 0;) {
			ProcessorOrder order = orders[(int) (Math.random() * orders.length)];
			beanContextFactory.registerBean(OrderedPostProcessor.class).propertyValue("Order", order);
		}
	}
}
