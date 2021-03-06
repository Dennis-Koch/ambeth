package com.koch.ambeth.ioc.injection;

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
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class InjectionTestModule implements IInitializingModule {
	public static final int BEAN_COUNT = 10000;

	public static final String NAME = "injectionTestBean-";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		for (int i = 0; i < BEAN_COUNT; i++) {
			int previousNumber = (BEAN_COUNT + i) % BEAN_COUNT;
			int counterpartNumber = ((int) (BEAN_COUNT * 1.5) + i) % BEAN_COUNT;
			String serviceName = NAME + i;
			String previousName = NAME + previousNumber;
			String counterpartName = NAME + counterpartNumber;
			beanContextFactory.registerBean(serviceName, InjectionTestBean.class)
					.propertyValue("Name", serviceName).propertyRef("Previous", previousName)
					.propertyRef("Counterpart", counterpartName);
		}
	}
}
