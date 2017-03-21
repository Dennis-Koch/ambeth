package com.koch.ambeth.persistence.event;

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

import java.util.List;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.util.ParamChecker;

@Service(IMultiEventService.class)
@PersistenceContext
public class MultiEventService implements IMultiEventService, IInitializingBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private ICache cache;

	protected IEntityFactory entityFactory;

	/**
	 * This service, but wrapped in proxies
	 */
	private IMultiEventService multiEventService;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(cache, "cache");
		ParamChecker.assertNotNull(entityFactory, "entityFactory");
		ParamChecker.assertNotNull(multiEventService, "multiEventService");
	}

	public void setCache(ICache cache) {
		this.cache = cache;
	}

	public void setEntityFactory(IEntityFactory entityFactory) {
		this.entityFactory = entityFactory;
	}

	public void setMultiEventService(IMultiEventService multiEventService) {
		this.multiEventService = multiEventService;
	}

	@Override
	public void save(List<MultiEventEntity> multiEventEntities) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void doMultipleThings(List<MultiEventEntity> multiEventEntities) {
		multiEventService.save(multiEventEntities);

		MultiEventEntity2 multiEventEntity2 = cache.getObject(MultiEventEntity2.class, 1);
		String name = multiEventEntity2.getName();
		name = name.replace(".1", ".2");
		multiEventEntity2.setName(name);
		multiEventService.save(multiEventEntity2);
	}

	@Override
	public void doMultipleThings2(List<MultiEventEntity> multiEventEntities) {
		MultiEventEntity newEntity = entityFactory.createEntity(MultiEventEntity.class);
		newEntity.setName("Name 4.2");
		multiEventEntities.add(newEntity);

		multiEventService.save(multiEventEntities);

		for (MultiEventEntity entity : multiEventEntities) {
			String name = entity.getName();
			name = name.replace(".2", ".3");
			entity.setName(name);
		}

		multiEventService.save(multiEventEntities);
	}

	@Override
	public void save(MultiEventEntity2 multiEventEntity2) {
		throw new UnsupportedOperationException();
	}
}
