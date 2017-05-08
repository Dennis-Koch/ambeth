package com.koch.ambeth.relations.many.lazy.fk.forward.none;

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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.merge.util.IPrefetchHandle;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.relations.AbstractRelationsTest;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestProperties;

@SQLData("Relations_data.sql")
@SQLStructure("Relations_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/relations/many/lazy/fk/forward/none/orm.xml")
public class ManyLazyFkForwardNoneRelationsTest extends AbstractRelationsTest {
	@Test
	public void testRetrieve() {
		EntityB entityB11 = cache.getObject(EntityB.class, 11);
		assertNotNull(entityB11);
		assertNotNull(entityB11.getEntityA());

		EntityB entityB12 = cache.getObject(EntityB.class, 12);
		assertNotNull(entityB12);
		assertNull(entityB12.getEntityA());
	}

	@Test
	public void testUpdateParent() {
		EntityB entityB = cache.getObject(EntityB.class, 11);
		entityB.setName(entityB.getName() + ".2");
		relationsService.save(entityB);
	}

	@Test
	public void testUpdateChild() {
		EntityB entityB = cache.getObject(EntityB.class, 11);
		EntityA entityA = entityB.getEntityA();
		String newName = entityA.getName() + ".2";
		entityA.setName(newName);
		relationsService.save(entityB);

		entityA = cache.getObject(EntityA.class, entityA.getId());
		assertEquals(newName, entityA.getName());
	}

	@Test
	public void testCreateRelated() {
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = entityFactory.createEntity(EntityA.class);
		entityA.setName("new EntityA");
		entityB.setEntityA(entityA);
		relationsService.save(entityB);

		assertTrue(entityA.getId() != 0);

		EntityA cachedEntityA = cache.getObject(EntityA.class, entityA.getId());
		assertNotNull(cachedEntityA);
	}

	@Test
	public void testAddRelated() {
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = entityFactory.createEntity(EntityA.class);
		entityA.setName("new EntityA");
		relationsService.save(entityA);

		assertTrue(entityA.getId() != 0);

		entityB.setEntityA(entityA);
		relationsService.save(entityB);

		EntityA cachedEntityA = cache.getObject(EntityA.class, entityA.getId());
		assertNotNull(cachedEntityA);
	}

	@Test
	public void testRemoveRelated() {
		EntityB entityB = cache.getObject(EntityB.class, 11);

		entityB.setEntityA(null);
		relationsService.save(entityB);

		EntityB cachedEntityB = cache.getObject(EntityB.class, 11);
		assertNull(cachedEntityB.getEntityA());
	}

	@Test
	public void testDeleteRelated() {
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = entityB.getEntityA();
		int id = entityA.getId();
		relationsService.delete(entityA);

		EntityA cachedEntityA = cache.getObject(EntityA.class, id);
		assertNull(cachedEntityA);

		EntityB cachedEntityB = cache.getObject(EntityB.class, 11);
		assertNull(cachedEntityB.getEntityA());
	}

	@Test
	public void testDeleteParent() {
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = entityB.getEntityA();
		int id = entityA.getId();

		relationsService.delete(entityB);

		EntityA cachedEntityA = cache.getObject(EntityA.class, id);
		assertNotNull(cachedEntityA);
		EntityB cachedEntityB = cache.getObject(EntityB.class, 11);
		assertNull(cachedEntityB);
	}

	@Test
	public void testPrefetchLazy() {
		EntityB entityB = cache.getObject(EntityB.class, 11);

		String propertyName = "EntityA";
		assertBeforePrefetch(entityB, propertyName);
		IPrefetchHandle prefetch = beanContext.getService(IPrefetchHelper.class).createPrefetch()
				.add(EntityB.class, propertyName).build();
		prefetch.prefetch(entityB);

		assertAfterPrefetch(entityB, propertyName);
		Assert.assertNotNull(entityB.getEntityA().getName());
	}

	@Test
	public void testMultipleAdd() {
		List<EntityB> entityBs = cache.getObjects(EntityB.class, 12, 13);
		EntityB entityB11 = cache.getObject(EntityB.class, 11);
		EntityA entityA = entityB11.getEntityA();

		for (EntityB entityB : entityBs) {
			entityB.setEntityA(entityA);
			relationsService.save(entityB);
		}
	}
}
