package com.koch.ambeth.persistence.multievent;

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

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;

@SQLData("MultiEvent_data.sql")
@SQLStructure("MultiEvent_structure.sql")
@TestModule(MultiEventTestModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/multievent/orm.xml")
public class MultiEventTest extends AbstractInformationBusWithPersistenceTest {
	@Autowired
	private ICacheFactory cacheFactory;

	@Autowired
	private IMultiEventService multiEventService;

	@Test
	public void testMultipleSaveCallsInOneMethod() throws Exception {
		IDisposableCache cache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE,
				"test");
		try {
			List<MultiEventEntity> multiEventEntities = cache.getObjects(MultiEventEntity.class,
					Arrays.asList(1, 2, 3));
			assertEquals(3, multiEventEntities.size());

			for (MultiEventEntity entity : multiEventEntities) {
				String name = entity.getName();
				name = name.replace(".1", ".2");
				entity.setName(name);
			}
			multiEventService.doMultipleThings(multiEventEntities);
		}
		finally {
			cache.dispose();
		}
		cache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, "test");
		try {
			List<MultiEventEntity> multiEventEntities = cache.getObjects(MultiEventEntity.class,
					Arrays.asList(1, 2, 3));
			for (MultiEventEntity entity : multiEventEntities) {
				String name = entity.getName();
				assertEquals("2", name.substring(name.length() - 1));
			}
		}
		finally {
			cache.dispose();
		}
	}

	@Test
	public void testChangeAndResaveInOneMethod() throws Exception {
		IDisposableCache cache = cacheFactory.create(CacheFactoryDirective.NoDCE, "test");
		try {
			List<MultiEventEntity> multiEventEntities = cache.getObjects(MultiEventEntity.class,
					Arrays.asList(1, 2, 3));
			assertEquals(3, multiEventEntities.size());

			for (MultiEventEntity entity : multiEventEntities) {
				String name = entity.getName();
				name = name.replace(".1", ".2");
				entity.setName(name);
			}
			multiEventService.doMultipleThings2(multiEventEntities);
		}
		finally {
			cache.dispose();
		}
		cache = cacheFactory.create(CacheFactoryDirective.NoDCE, "test");
		try {
			List<MultiEventEntity> multiEventEntities = cache.getObjects(MultiEventEntity.class,
					Arrays.asList(1, 2, 3));
			for (MultiEventEntity entity : multiEventEntities) {
				String name = entity.getName();
				assertEquals("3", name.substring(name.length() - 1));
			}
		}
		finally {
			cache.dispose();
		}
	}
}
