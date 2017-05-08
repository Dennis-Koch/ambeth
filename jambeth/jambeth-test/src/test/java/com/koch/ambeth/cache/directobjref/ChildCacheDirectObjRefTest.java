package com.koch.ambeth.cache.directobjref;

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
import static org.junit.Assert.assertSame;

import java.util.Arrays;

import org.junit.Test;

import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.cache.transfer.LoadContainer;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.transfer.DirectObjRef;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.service.ProcessServiceTestModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.IList;

@SQLStructure("ChildCacheDirectObjRefTest_structure.sql")
@SQLData("ChildCacheDirectObjRefTest_data.sql")
@TestModule(ProcessServiceTestModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml")
public class ChildCacheDirectObjRefTest extends AbstractInformationBusWithPersistenceTest {
	protected ICache fixture;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(fixture, "cache");
	}

	public void setFixture(ICache fixture) {
		this.fixture = fixture;
	}

	@Test
	public void testGetObject_ObjRef_normal() {
		IObjRef ori = new ObjRef(Material.class, 1, 1);
		Material actual = (Material) fixture.getObject(ori, CacheDirective.none());
		assertNotNull(actual);
		assertEquals(1, actual.getId());
	}

	@Test
	public void testGetObject_ObjRef_CacheValue() {
		IObjRef ori = new ObjRef(Material.class, 1, 1);
		RootCacheValue actual =
				(RootCacheValue) fixture.getObject(ori, CacheDirective.cacheValueResult());
		assertNotNull(actual);
		assertEquals(1, actual.getId());
	}

	@Test
	public void testGetObject_ObjRef_LoadContainer() {
		IObjRef ori = new ObjRef(Material.class, 1, 1);
		LoadContainer actual =
				(LoadContainer) fixture.getObject(ori, CacheDirective.loadContainerResult());
		assertNotNull(actual);
		assertEquals(1, actual.getReference().getId());
	}

	@Test
	public void testGetObject_DirectObjRef_normal() {
		IDirectObjRef dori = getDORI();
		Material actual = (Material) fixture.getObject(dori, CacheDirective.none());
		assertNotNull(actual);
		assertSame(dori.getDirect(), actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetObject_DirectObjRef_CacheValue() {
		IObjRef dori = getDORI();
		fixture.getObject(dori, CacheDirective.cacheValueResult());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetObject_DirectObjRef_LoadContainer() {
		IObjRef dori = getDORI();
		fixture.getObject(dori, CacheDirective.loadContainerResult());
	}

	@Test
	public void testGetObjects_normal() {
		IObjRef ori = new ObjRef(Material.class, 1, 1);
		IDirectObjRef dori = getDORI();

		IList<Object> actuals = fixture.getObjects(Arrays.asList(ori, dori), CacheDirective.none());

		assertNotNull(actuals);
		assertEquals(2, actuals.size());

		Material actual = (Material) actuals.get(0);
		assertNotNull(actual);
		assertEquals(1, actual.getId());

		actual = (Material) actuals.get(1);
		assertNotNull(actual);
		assertSame(dori.getDirect(), actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetObjects_CacheValue() {
		IObjRef ori = new ObjRef(Material.class, 1, 1);
		IObjRef dori = getDORI();

		fixture.getObjects(Arrays.asList(ori, dori), CacheDirective.cacheValueResult());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetObjects_LoadContainer() {
		IObjRef ori = new ObjRef(Material.class, 1, 1);
		IObjRef dori = getDORI();

		fixture.getObjects(Arrays.asList(ori, dori), CacheDirective.loadContainerResult());
	}

	protected IDirectObjRef getDORI() {
		Material newMaterial = entityFactory.createEntity(Material.class);
		newMaterial.setBuid("direct buid");
		IDirectObjRef dori = new DirectObjRef(Material.class, newMaterial);
		return dori;
	}
}
