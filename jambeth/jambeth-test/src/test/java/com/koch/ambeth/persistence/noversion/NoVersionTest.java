package com.koch.ambeth.persistence.noversion;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.koch.ambeth.cache.config.CacheConfigurationConstants;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.noversion.models.INoVersionService;
import com.koch.ambeth.persistence.noversion.models.NoVersion;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.ParamChecker;

@TestModule(NoVersionTestModule.class)
@TestPropertiesList({
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseBehaviourStrict,
				value = "true"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseTableIgnore,
				value = "NO_VERSION_BACKING"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile,
				value = "com/koch/ambeth/persistence/noversion/orm.xml"),
		@TestProperties(name = CacheConfigurationConstants.FirstLevelCacheType, value = "PROTOTYPE")})
@SQLStructure("NoVersion_structure.sql")
@SQLData("NoVersion_data.sql")
public class NoVersionTest extends AbstractInformationBusWithPersistenceTest {
	protected ICache cache;

	protected ICacheFactory cacheFactory;

	protected INoVersionService noVersionService;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(cache, "cache");
		ParamChecker.assertNotNull(cacheFactory, "cacheFactory");
		ParamChecker.assertNotNull(noVersionService, "noVersionService");
	}

	public void setCache(ICache cache) {
		this.cache = cache;
	}

	public void setCacheFactory(ICacheFactory cacheFactory) {
		this.cacheFactory = cacheFactory;
	}

	public void setNoVersionService(INoVersionService noVersionService) {
		this.noVersionService = noVersionService;
	}

	@Test
	public void testCreate() throws Exception {
		NoVersion entity = entityFactory.createEntity(NoVersion.class);
		entity.setName("zehn tausend");
		noVersionService.create(entity);
	}

	@Test
	public void testUpdate() throws Exception {
		String newName = "eins punkt zwei";
		NoVersion entity = cache.getObject(NoVersion.class, 1);
		assertFalse(newName.equals(entity.getName()));
		entity.setName(newName);
		noVersionService.update(entity);

		NoVersion actual = cache.getObject(NoVersion.class, 1);
		assertTrue(newName.equals(actual.getName()));
	}

	@Test
	public void testDelete() throws Exception {
		NoVersion entity = cache.getObject(NoVersion.class, 1);
		assertNotNull(entity);

		noVersionService.delete(entity);

		NoVersion actual = cache.getObject(NoVersion.class, 1);
		assertNull(actual);
	}

	@Test
	public void testQuery() throws Exception {
		IQueryBuilder<NoVersion> qb = queryBuilderFactory.create(NoVersion.class);
		IQuery<NoVersion> query = qb.build(qb.isEqualTo(qb.property("Name"), qb.value("zwei")));
		List<NoVersion> result = query.retrieve();
		assertEquals(1, result.size());
		NoVersion actual = result.get(0);
		assertEquals(2, actual.getId());
		assertEquals("zwei", actual.getName());
	}

	@Test
	public void testUpdateWithoutOptimisticLocking() throws Exception {
		String newName1 = "eins punkt zwei";
		String newName2 = "eins punkt drei";
		NoVersion entityInst1 = cache.getObject(NoVersion.class, 1);
		NoVersion entityInst2 = cacheFactory.create(CacheFactoryDirective.SubscribeGlobalDCE, "test")
				.getObject(NoVersion.class, 1);
		assertNotSame(entityInst1, entityInst2);
		assertFalse(newName1.equals(entityInst1.getName()));
		assertFalse(newName2.equals(entityInst1.getName()));

		entityInst1.setName(newName1);
		noVersionService.update(entityInst1);

		NoVersion actual1 = cacheFactory.create(CacheFactoryDirective.SubscribeGlobalDCE, "test")
				.getObject(NoVersion.class, 1);
		assertEquals(newName1, actual1.getName());

		entityInst2.setName(newName2);
		noVersionService.update(entityInst2);

		NoVersion actual2 = cacheFactory.create(CacheFactoryDirective.SubscribeGlobalDCE, "test")
				.getObject(NoVersion.class, 1);
		assertEquals(newName2, actual2.getName());
	}
}
