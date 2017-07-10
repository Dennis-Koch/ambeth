package com.koch.ambeth.mapping;

/*-
 * #%L
 * jambeth-mapping
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
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.garbageproxy.IGarbageProxyConstructor;
import com.koch.ambeth.ioc.garbageproxy.IGarbageProxyFactory;

public class MapperServiceFactory implements IMapperServiceFactory, IInitializingBean {
	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IGarbageProxyFactory garbageProxyFactory;

	protected IGarbageProxyConstructor<IMapperService> mapperServiceGPC;

	@Override
	public void afterPropertiesSet() throws Throwable {
		mapperServiceGPC = garbageProxyFactory.createGarbageProxyConstructor(IMapperService.class);
	}

	@Override
	public IMapperService create() {
		IMapperService mapperService = beanContext.registerBean(ModelTransferMapper.class).finish();
		return mapperServiceGPC.createInstance(mapperService);
	}
}
