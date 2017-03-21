package com.koch.ambeth.persistence.jdbc.splitloading;

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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;

@SQLData("SplitLoading_data.sql")
@SQLStructure("SplitLoading_structure.sql")
@TestModule(TestServicesModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/jdbc/splitloading/orm.xml")
public class SplitLoadingTest extends AbstractInformationBusWithPersistenceTest
{
	@Test
	public void testDataSetup()
	{
		IRefEntityService service = beanContext.getService(IRefEntityService.class);
		ICache cache = beanContext.getService(ICache.class);

		int entityCount = 1001;

		List<RefEntity> entities = new ArrayList<RefEntity>(entityCount);
		RefEntity toSetLast = entityFactory.createEntity(RefEntity.class);
		RefEntity last = toSetLast;
		for (int i = 1; i < entityCount; i++)
		{
			RefEntity entity = entityFactory.createEntity(RefEntity.class);
			entity.setOther(last);
			entities.add(entity);
			last = entity;
		}
		toSetLast.setOther(last);
		entities.add(toSetLast);

		service.save(entities);

		List<Integer> ids = new ArrayList<Integer>(entityCount);
		for (RefEntity entity : entities)
		{
			ids.add(entity.getId());
		}

		List<RefEntity> actuals = cache.getObjects(RefEntity.class, ids);
		assertEquals(ids.size(), actuals.size());
		for (RefEntity actual : actuals)
		{
			assertNotNull(actual);
		}
	}
}
