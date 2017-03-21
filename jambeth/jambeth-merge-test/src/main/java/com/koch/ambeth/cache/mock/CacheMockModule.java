package com.koch.ambeth.cache.mock;

/*-
 * #%L
 * jambeth-merge-test
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
import com.koch.ambeth.merge.IRevertChangesHelper;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.ICacheProvider;
import com.koch.ambeth.merge.util.IPrefetchHelper;

/**
 * Support for unit tests that do not include jAmbeth.Cache
 */
public class CacheMockModule implements IInitializingModule {
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean("revertChangesHelper", RevertChangesHelperMock.class)
				.autowireable(IRevertChangesHelper.class);
		beanContextFactory.registerBean("cache", CacheMock.class).autowireable(ICache.class);
		beanContextFactory.registerBean("cacheFactory", CacheFactoryMock.class)
				.autowireable(ICacheFactory.class);
		beanContextFactory.registerBean("cacheProvider", CacheProviderMock.class)
				.autowireable(ICacheProvider.class);
		beanContextFactory.registerBean("prefetchHelper", PrefetchHelperMock.class)
				.autowireable(IPrefetchHelper.class);
	}
}
