package com.koch.ambeth.persistence.update;

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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.service.proxy.Service;

@Service(EntityCService.class)
@PersistenceContext
public class EntityCService implements IEntityCService, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(EntityCService.class)
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
	}

	@Override
	public void save(EntityC entity)
	{
		throw new UnsupportedOperationException();
	}
}
