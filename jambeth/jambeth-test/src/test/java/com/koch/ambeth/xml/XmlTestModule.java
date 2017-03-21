package com.koch.ambeth.xml;

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

public class XmlTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		// beanContextFactory.registerBean("proxyHelper", DefaultProxyHelper.class).autowireable(IProxyHelper.class);
		//
		// beanContextFactory.registerBean("typeInfoProvider", TypeInfoProvider.class).autowireable(ITypeInfoProvider.class);
		//
		// beanContextFactory.registerBean("relationProvider", RelationProvider.class).autowireable(IRelationProvider.class);
		//
		// beanContextFactory.registerBean("entityMetaDataProviderDummy", EntityMetaDataProviderDummy.class).autowireable(IEntityMetaDataProvider.class);
		//
		// beanContextFactory.registerBean("cache", CacheDummy.class).autowireable(ICache.class);
		//
		// beanContextFactory.registerBean("oriHelper", OriHelperDummy.class).autowireable(IObjRefHelper.class);
		//
		// beanContextFactory.registerBean("entityFactory", EntityFactoryDummy.class).autowireable(IEntityFactory.class);
		//
		// beanContextFactory.registerBean("cacheFactory", CacheFactoryDummy.class).autowireable(ICacheFactory.class);
		//
		// beanContextFactory.registerBean("mergeController", MergeControllerDummy.class).autowireable(IMergeController.class);
		//
		// beanContextFactory.registerBean("prefetchHelper", PrefetchHelperDummy.class).autowireable(IPrefetchHelper.class);
	}
}
