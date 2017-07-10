package com.koch.ambeth.testutil.contextstore;

/*-
 * #%L
 * jambeth-information-bus-test
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

public class ServiceContextStoreTestModule implements IInitializingModule {
	public static final String BLA_DSERVICE = "blaDService";

	public static final String BLA_PROV_EMPTY = "blaDServiceProviderEmpty";

	public static final String BLA_PROV_1 = "blaDServiceProvider1";

	public static final String BLA_PROV_2 = "blaDServiceProvider2";

	public static final String BLA_PROV_3 = "blaDServiceProvider3";

	public static final String BLA_PROV_4 = "blaDServiceProvider4";

	public static final String BLA_PROV_5 = "blaDServiceProvider5";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean(BLA_DSERVICE, BlaDServiceImpl.class)
				.autowireable(BlaDService.class);

		beanContextFactory.registerBean(BLA_PROV_EMPTY, BlaDServiceProviderImpl.class);
		beanContextFactory.registerBean(BLA_PROV_1, BlaDServiceProviderImpl.class);
		beanContextFactory.registerBean(BLA_PROV_2, BlaDServiceProviderImpl.class)
				.autowireable(BlaDServiceProvider.class);
		beanContextFactory.registerBean(BLA_PROV_3, BlaDServiceProviderImpl.class);
		beanContextFactory.registerBean(BLA_PROV_4, BlaDServiceProviderImpl.class)
				.autowireable(BlaDServiceProviderImpl.class);
		beanContextFactory.registerBean(BLA_PROV_5, BlaDServiceProviderImpl.class);
	}
}
