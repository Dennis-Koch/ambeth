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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.EnumSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.cache.ChildCache;
import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheProvider;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.IPrefetchHandle;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.persistence.update.RelationUpdateTest.RelationUpdateTestModule;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.IList;

@SQLData("RelationUpdate_data.sql")
@SQLStructure("RelationUpdate_structure.sql")
@TestModule(RelationUpdateTestModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/update/orm.xml")
public class RelationUpdateTest extends AbstractInformationBusWithPersistenceTest
{

	public static class RelationUpdateTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean("entityAService", EntityAService.class).autowireable(IEntityAService.class);
			beanContextFactory.registerBean("entityCService", EntityCService.class).autowireable(IEntityCService.class);
		}
	};

	protected ICache cache;

	protected IEntityAService entityAService;

	protected IEntityCService entityCService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(cache, "cache");
		ParamChecker.assertNotNull(entityAService, "entityAService");
		ParamChecker.assertNotNull(entityCService, "entityCService");
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	public void setEntityAService(IEntityAService entityAService)
	{
		this.entityAService = entityAService;
	}

	public void setEntityCService(IEntityCService entityCService)
	{
		this.entityCService = entityCService;
	}

	@Test
	public void testSetup() throws Exception
	{
		EntityA entityA = cache.getObject(EntityA.class, 1);
		assertNotNull(entityA);
		assertNotNull(entityA.getOther());
		assertEquals(12, entityA.getOther().getId());
		assertNotNull(entityA.getEntityCs());
		assertEquals(3, entityA.getEntityCs().size());

		EntityB entityB2 = cache.getObject(EntityB.class, 12);
		assertNotNull(entityB2);
		assertNotNull(entityB2.getOther());
		assertEquals(1, entityB2.getOther().getId());

		EntityB entityB3 = cache.getObject(EntityB.class, 13);
		assertNotNull(entityB3);
		assertNull(entityB3.getOther());

		EntityC entityC1 = cache.getObject(EntityC.class, 21);
		assertNotNull(entityC1);
		assertNotNull(entityC1.getOther());
		assertEquals(1, entityC1.getOther().getId());
	}

	@Test
	public void testOneToOneUpdate() throws Exception
	{
		EntityA entityA = cache.getObject(EntityA.class, 1);
		EntityB entityB3 = cache.getObject(EntityB.class, 13);

		entityA.setOther(entityB3);
		entityAService.save(entityA);

		EntityA actualA = cache.getObject(EntityA.class, 1);
		assertNotNull(actualA.getOther());
		assertEquals(13, actualA.getOther().getId());

		EntityB actualB2 = cache.getObject(EntityB.class, 12);
		assertNull(actualB2.getOther());

		EntityB actualB3 = cache.getObject(EntityB.class, 13);
		assertNotNull(actualB3.getOther());
		assertEquals(1, actualB3.getOther().getId());
	}

	@Test
	public void testOneToManyUpdate1() throws Exception
	{
		EntityA entityA = cache.getObject(EntityA.class, 1);
		EntityC entityC1 = cache.getObject(EntityC.class, 21);

		assertEquals(3, entityA.getEntityCs().size());
		entityA.getEntityCs().remove(entityC1);
		assertEquals(2, entityA.getEntityCs().size());
		entityAService.save(entityA);

		EntityA actualA = cache.getObject(EntityA.class, 1);
		assertEquals(2, actualA.getEntityCs().size());

		EntityC actualC1 = cache.getObject(EntityC.class, 21);
		assertNull(actualC1.getOther());
	}

	@Test
	public void testOneToManyUpdate2() throws Exception
	{
		EntityA entityA = cache.getObject(EntityA.class, 1);

		assertEquals(3, entityA.getEntityCs().size());
		EntityC removed = entityA.getEntityCs().remove(0);
		assertEquals(2, entityA.getEntityCs().size());
		entityAService.save(entityA);

		EntityA actualA = cache.getObject(EntityA.class, 1);
		assertEquals(2, actualA.getEntityCs().size());

		EntityC actualC1 = cache.getObject(EntityC.class, removed.getId());
		assertNull(actualC1.getOther());
	}

	@Test
	public void testManyToOneUpdate() throws Exception
	{
		EntityC entityC = cache.getObject(EntityC.class, 21);

		EntityA entityA = entityC.getOther();
		assertEquals(3, entityA.getEntityCs().size());

		entityC.setOther(null);
		entityCService.save(entityC);

		EntityA actualA = cache.getObject(EntityA.class, 1);
		assertEquals(2, actualA.getEntityCs().size());

		EntityC actualC1 = cache.getObject(EntityC.class, 21);
		assertNull(actualC1.getOther());
	}

	@Test
	public void testRelationsWithNewObjects() throws Exception
	{
		EntityA entityA = entityFactory.createEntity(EntityA.class);
		entityA.setOther(entityFactory.createEntity(EntityB.class));
		// entityA.setEntityCs(new ArrayList<EntityC>());
		entityA.getEntityCs().add(entityFactory.createEntity(EntityC.class));
		entityA.getEntityCs().add(entityFactory.createEntity(EntityC.class));
		entityA.getEntityCs().add(entityFactory.createEntity(EntityC.class));
		entityA.getEntityCs().add(entityFactory.createEntity(EntityC.class));

		entityAService.save(entityA);

		EntityA actualA = cache.getObject(EntityA.class, entityA.getId());
		assertNotNull(actualA.getOther());
		assertProxyEquals(actualA, actualA.getOther().getOther());
		List<EntityC> entityCs = actualA.getEntityCs();
		assertEquals(4, entityCs.size());
		for (int i = entityCs.size(); i-- > 0;)
		{
			assertProxyEquals(actualA, entityCs.get(i).getOther());
		}
	}

	@Test
	public void testRemoveAndReSetEntityD() throws Exception
	{
		EntityA entity = cache.getObject(EntityA.class, 1);
		entityAService.removeAndReSetEntityD(entity);
	}

	@Test
	public void testObjRelFromRootCache() throws Exception
	{
		int entityCsIndex = entityMetaDataProvider.getMetaData(EntityA.class).getIndexByRelationName("EntityCs");
		ICacheProvider cacheProvider = beanContext.getService(ICacheProvider.class);
		ChildCache cache = (ChildCache) cacheProvider.getCurrentCache();
		EntityA entity = cache.getObject(EntityA.class, 1);
		RootCacheValue rcv = (RootCacheValue) cache.getObject(new ObjRef(EntityA.class, 1, null),
				EnumSet.of(CacheDirective.FailInCacheHierarchy, CacheDirective.CacheValueResult));
		Assert.assertNull(rcv.getRelation(entityCsIndex));
		entity.getEntityCs().size();
		Assert.assertNotNull(rcv.getRelation(entityCsIndex));
		cache.clear();
		EntityA entity2 = cache.getObject(EntityA.class, 1);
		Assert.assertNotSame(entity, entity2);
		entity.getEntityCs().size();
	}

	@Test
	public void testObjRelBatchLoad() throws Exception
	{
		IQueryBuilder<EntityA> qb = queryBuilderFactory.create(EntityA.class);
		IQuery<EntityA> query = qb.build();
		IList<EntityA> resultA = query.retrieve();
		IPrefetchHelper prefetchHelper = beanContext.getService(IPrefetchHelper.class);
		IPrefetchHandle prefetch = prefetchHelper.createPrefetch().add(EntityA.class, "EntityCs").build();
		prefetch.prefetch(resultA);

	}
}
