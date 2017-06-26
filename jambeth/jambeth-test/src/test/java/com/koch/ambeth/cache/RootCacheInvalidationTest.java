package com.koch.ambeth.cache;

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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.EnumSet;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheContext;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.model.MaterialGroup;
import com.koch.ambeth.model.Unit;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.service.IMaterialService;
import com.koch.ambeth.service.TestServicesModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

@TestModule({TestServicesModule.class})
@SQLStructure("../persistence/jdbc/JDBCDatabase_structure.sql")
@SQLData("../persistence/jdbc/Example_data.sql")
@TestPropertiesList({
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml")})
public class RootCacheInvalidationTest extends AbstractInformationBusWithPersistenceTest {
	protected ICacheContext cacheContext;

	protected ICacheFactory cacheFactory;

	protected ICache fixture;

	protected IMaterialService materialService;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(cacheContext, "CacheContext");
		ParamChecker.assertNotNull(cacheFactory, "CacheFactory");
		ParamChecker.assertNotNull(materialService, "MaterialService");
		ParamChecker.assertNotNull(fixture, "Fixture");
	}

	public void setCacheContext(ICacheContext cacheContext) {
		this.cacheContext = cacheContext;
	}

	public void setCacheFactory(ICacheFactory cacheFactory) {
		this.cacheFactory = cacheFactory;
	}

	public void setFixture(ICache fixture) {
		this.fixture = fixture;
	}

	public void setMaterialService(IMaterialService materialService) {
		this.materialService = materialService;
	}

	@Test
	public void testRootCacheDataChangePerformance() throws Throwable {
		final IDisposableCache cache =
				cacheFactory.create(CacheFactoryDirective.SubscribeGlobalDCE, "test");
		cacheContext.executeWithCache(cache, new IResultingBackgroundWorkerDelegate<Object>() {
			@Override
			public Object invoke() throws Exception {
				MaterialGroup mg = cache.getObject(MaterialGroup.class, "pl");
				Unit unit = cache.getObject(Unit.class, (long) 1);
				IList<Material> materials = new ArrayList<>();
				for (int a = 100; a-- > 0;) {
					Material material = entityFactory.createEntity(Material.class);
					material.setName("new material");
					material.setMaterialGroup(mg);
					material.setUnit(unit);
					materials.add(material);
				}
				materialService.updateMaterials(materials.toArray(Material.class));
				for (int a = materials.size(); a-- > 0;) {
					Material material = materials.get(a);
					material.setName(material.getName() + "2");
				}
				materialService.updateMaterials(materials.toArray(Material.class));
				return null;
			}
		});
	}

	@Test
	public void testRootCacheInvalidation() {
		MaterialGroup mg = fixture.getObject(MaterialGroup.class, "pl");
		Unit unit = fixture.getObject(Unit.class, (long) 1);
		rootCacheInvalidation(mg, unit, false);
	}

	@Test
	public void testRootCacheInvalidation2() throws Throwable {
		final IDisposableCache cache =
				cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, "test");
		cacheContext.executeWithCache(cache, new IResultingBackgroundWorkerDelegate<Object>() {
			@Override
			public Object invoke() throws Exception {
				MaterialGroup mg = cache.getObject(MaterialGroup.class, "pl");
				Unit unit = cache.getObject(Unit.class, (long) 1);
				rootCacheInvalidation(mg, unit, false);
				return null;
			}
		});
	}

	protected void rootCacheInvalidation(MaterialGroup mg, Unit unit, boolean mustBeNull) {
		assertNotNull(mg);
		assertNotNull(unit);

		Material material = entityFactory.createEntity(Material.class);
		material.setName("new material");
		material.setMaterialGroup(mg);
		material.setUnit(unit);
		materialService.updateMaterial(material);

		Object hardRef = fixture.getObject(new ObjRef(Material.class, material.getId(), null),
				CacheDirective.cacheValueResult());
		assertNotNull(hardRef);

		Object object = fixture.getObject(new ObjRef(Material.class, material.getId(), null),
				EnumSet.of(CacheDirective.FailInCacheHierarchy, CacheDirective.CacheValueResult));

		assertNotNull(object);
		Assert.assertSame(object, hardRef);

		material.setName("updated material");
		materialService.updateMaterial(material);

		Object object2 = fixture.getObject(new ObjRef(Material.class, material.getId(), null),
				EnumSet.of(CacheDirective.FailInCacheHierarchy, CacheDirective.CacheValueResult));

		if (mustBeNull) {
			assertNull(object2);
		}
		else {
			assertNotNull(object2);
		}
	}
}
