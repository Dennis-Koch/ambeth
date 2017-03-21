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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.ParamChecker;

@SQLData("MultiEvent_data.sql")
@SQLStructure("MultiEvent_structure.sql")
@TestModule(MultiEventTestModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/event/orm.xml")
public class MultiEventTest extends AbstractInformationBusWithPersistenceTest
{
	private ICacheFactory cacheFactory;

	private IMultiEventService multiEventService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(cacheFactory, "cacheFactory");
		ParamChecker.assertNotNull(multiEventService, "multiEventService");
	}

	public void setCacheFactory(ICacheFactory cacheFactory)
	{
		this.cacheFactory = cacheFactory;
	}

	public void setMultiEventService(IMultiEventService multiEventService)
	{
		this.multiEventService = multiEventService;
	}

	@Test
	public void testMultipleSaveCallsInOneMethod() throws Exception
	{
		ICache cache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, "test");
		List<MultiEventEntity> multiEventEntities = cache.getObjects(MultiEventEntity.class, Arrays.asList(1, 2, 3));
		assertEquals(3, multiEventEntities.size());

		for (MultiEventEntity entity : multiEventEntities)
		{
			String name = entity.getName();
			name = name.replace(".1", ".2");
			entity.setName(name);
		}
		multiEventService.doMultipleThings(multiEventEntities);

		cache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, "test");
		multiEventEntities = cache.getObjects(MultiEventEntity.class, Arrays.asList(1, 2, 3));
		for (MultiEventEntity entity : multiEventEntities)
		{
			String name = entity.getName();
			assertEquals("2", name.substring(name.length() - 1));
		}
	}

	@Test
	public void testChangeAndResaveInOneMethod() throws Exception
	{
		ICache cache = cacheFactory.create(CacheFactoryDirective.NoDCE, "test");
		List<MultiEventEntity> multiEventEntities = cache.getObjects(MultiEventEntity.class, Arrays.asList(1, 2, 3));
		assertEquals(3, multiEventEntities.size());

		for (MultiEventEntity entity : multiEventEntities)
		{
			String name = entity.getName();
			name = name.replace(".1", ".2");
			entity.setName(name);
		}
		multiEventService.doMultipleThings2(multiEventEntities);

		cache = cacheFactory.create(CacheFactoryDirective.NoDCE, "test");
		multiEventEntities = cache.getObjects(MultiEventEntity.class, Arrays.asList(1, 2, 3));
		for (MultiEventEntity entity : multiEventEntities)
		{
			String name = entity.getName();
			assertEquals("3", name.substring(name.length() - 1));
		}
	}
}
