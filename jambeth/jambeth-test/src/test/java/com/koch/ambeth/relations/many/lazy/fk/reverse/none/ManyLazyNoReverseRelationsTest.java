package com.koch.ambeth.relations.many.lazy.fk.reverse.none;

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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.util.IPrefetchHandle;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.relations.AbstractRelationsTest;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;

@SQLData("Relations_data.sql")
@SQLStructure("Relations_structure.sql")
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.ToManyDefaultCascadeLoadMode, value = "LAZY"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/relations/many/lazy/fk/reverse/none/orm.xml") })
public class ManyLazyNoReverseRelationsTest extends AbstractRelationsTest
{
	@Test
	public void testRetrieve()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);
		assertNotNull(entityB);
		assertEquals(2, entityB.getEntityAs().size());
	}

	@Test
	public void testUpdateParent()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);
		entityB.setName(entityB.getName() + ".2");
		relationsService.save(entityB);
	}

	@Test
	public void testCreateRelated()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = entityFactory.createEntity(EntityA.class);
		entityA.setName("new EntityA");
		entityB.getEntityAs().add(entityA);
		relationsService.save(entityB);

		assertTrue(entityA.getId() != 0);

		EntityA cachedEntityA = cache.getObject(EntityA.class, entityA.getId());
		assertNotNull(cachedEntityA);
	}

	@Test
	public void testAddRelated()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = entityFactory.createEntity(EntityA.class);
		entityA.setName("new EntityA");
		relationsService.save(entityA);

		assertTrue(entityA.getId() != 0);

		entityB.getEntityAs().add(entityA);
		relationsService.save(entityB);

		EntityA cachedEntityA = cache.getObject(EntityA.class, entityA.getId());
		assertNotNull(cachedEntityA);
	}

	@Test
	public void testRemoveRelated()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		entityB.getEntityAs().remove(0);
		relationsService.save(entityB);

		EntityB cachedEntityB = cache.getObject(EntityB.class, 11);
		assertEquals(1, cachedEntityB.getEntityAs().size());
	}

	@Test
	public void testDeleteRelated()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = entityB.getEntityAs().get(0);
		int id = entityA.getId();
		relationsService.delete(entityA);

		EntityA cachedEntityA = cache.getObject(EntityA.class, id);
		assertNull(cachedEntityA);

		EntityB cachedEntityB = cache.getObject(EntityB.class, 11);
		assertEquals(1, cachedEntityB.getEntityAs().size());
	}

	@Test
	public void testDeleteParent()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = entityB.getEntityAs().get(0);
		int id = entityA.getId();

		relationsService.delete(entityB);

		EntityA cachedEntityA = cache.getObject(EntityA.class, id);
		assertNotNull(cachedEntityA);
		EntityB cachedEntityB = cache.getObject(EntityB.class, 11);
		assertNull(cachedEntityB);
	}

	@Test
	public void testMoveChildrenToOtherParent()
	{
		EntityB entityB_src = cache.getObject(EntityB.class, 11);
		EntityB entityB_target = cache.getObject(EntityB.class, 12);

		entityB_target.setEntityAs(entityB_src.getEntityAs());

		relationsService.save(Arrays.asList(entityB_src, entityB_target));
		List<EntityA> entityAs_src = entityB_src.getEntityAs();
		List<EntityA> entityAs_target = entityB_target.getEntityAs();

		Assert.assertEquals(0, entityAs_src.size());
		Assert.assertEquals(2, entityAs_target.size());
	}

	@Test
	public void testPrefetchLazy()
	{
		String propertyName = "EntityAs";
		EntityB entityB = cache.getObject(EntityB.class, 12);

		int relationIndex = ((IObjRefContainer) entityB).get__EntityMetaData().getIndexByRelationName(propertyName);

		Assert.assertTrue(!((IObjRefContainer) entityB).is__Initialized(relationIndex));
		IPrefetchHandle prefetch = beanContext.getService(IPrefetchHelper.class).createPrefetch().add(EntityB.class, propertyName).build();
		prefetch.prefetch(entityB);

		Assert.assertTrue(((IObjRefContainer) entityB).is__Initialized(relationIndex));
		Assert.assertEquals(0, entityB.getEntityAs().size());
	}
}
