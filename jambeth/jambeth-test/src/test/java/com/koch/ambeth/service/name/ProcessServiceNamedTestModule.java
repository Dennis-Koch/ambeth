package com.koch.ambeth.service.name;

/*-
 * #%L
 * jambeth-test
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
import com.koch.ambeth.service.TestService;

public class ProcessServiceNamedTestModule implements IInitializingModule {
	public static final String TEST_SERVICE_NAME = "testService";

	public static final String TEST_SERVICE_2_NAME = "testService2";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean(TEST_SERVICE_NAME, TestService.class)
				.autowireable(com.koch.ambeth.transfer.ITestService.class);

		beanContextFactory.registerBean(TEST_SERVICE_2_NAME, TestService2.class)
				.autowireable(ITestService.class);
	}
}
