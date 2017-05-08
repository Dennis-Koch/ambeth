package com.koch.ambeth.persistence.jdbc;

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
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.koch.ambeth.ObjectMother;
import com.koch.ambeth.cache.config.CacheConfigurationConstants;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.service.IMergeService;
import com.koch.ambeth.merge.transfer.CUDResult;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.DirectObjRef;
import com.koch.ambeth.merge.transfer.PrimitiveUpdateItem;
import com.koch.ambeth.merge.transfer.RelationUpdateItem;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.model.MaterialGroup;
import com.koch.ambeth.model.Unit;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.DatabaseCallback;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.service.IMaterialService;
import com.koch.ambeth.service.TestServicesModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ILinkedMap;

@SQLData("Example_data.sql")
@SQLStructure("JDBCDatabase_structure.sql")
@TestModule(TestServicesModule.class)
@TestPropertiesList({
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml"),
		@TestProperties(name = CacheConfigurationConstants.FirstLevelCacheType, value = "PROTOTYPE")})
public class JDBCDatabaseTest extends AbstractInformationBusWithPersistenceTest {
	@Autowired
	private ICache cache;

	@Autowired
	private IMaterialService materialService;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(cache, "cache");
		ParamChecker.assertNotNull(materialService, "materialService");
	}

	@Test
	public void testSetupFramework() {
		IMaterialService materialService = beanContext.getService(IMaterialService.class);
		List<Material> materials = materialService.getAllMaterials();
		assertEquals("Loop function does not work or test setup has been changed!", 55,
				materials.size());
	}

	@Test
	public void testPrepareStatementWithFlag() throws SQLException {
		transaction.processAndCommit(new DatabaseCallback() {

			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
					throws Exception {
				Connection conn = beanContext.getService(Connection.class);
				PreparedStatement prep = conn.prepareStatement("SELECT 1", Statement.RETURN_GENERATED_KEYS);
				prep.close();
			}
		});
	}

	@Test
	public void test() throws Throwable {
		transaction.processAndCommit(new DatabaseCallback() {
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) {
				// ITable materialTable = database.getDatabase().getTableByType(Material.class);
				// IVersionItem findSingle = materialTable.getFieldByMemberName("Name").findSingle("Mein
				// Material");
			}
		});
		ICacheFactory cacheFactory = beanContext.getService(ICacheFactory.class);

		IDisposableCache cache1 = cacheFactory.create(CacheFactoryDirective.SubscribeGlobalDCE, "test");
		IDisposableCache cache2 = cacheFactory.create(CacheFactoryDirective.SubscribeGlobalDCE, "test");
		try {
			Material material = cache1.getObject(Material.class, 1);
			Material material2 = cache2.getObject(Material.class, 1);

			Assert.assertTrue("Reference is null", material != null);
			Assert.assertTrue("Reference is null", material2 != null);
			Assert.assertTrue("References are equal", material != material2);
		}
		finally {
			cache2.dispose();
			cache1.dispose();
		}
	}

	@Test
	public void testReferenceEquals() throws Throwable {
		ICache cache = beanContext.getService(ICache.class);

		Material material = cache.getObject(Material.class, 1);
		Material material2 = cache.getObject(Material.class, 1);

		Assert.assertTrue("Reference is null", material != null);
		Assert.assertTrue("Reference is null", material2 != null);
		Assert.assertTrue("References must not be identical", material != material2);
	}

	@Test
	public void testObjectEquals() throws Throwable {
		ICache cache = beanContext.getService(ICache.class);

		Material material = cache.getObject(Material.class, 1);
		Material material2 = cache.getObject(Material.class, 1);

		Assert.assertNotNull("Object must be valid", material);
		Assert.assertNotNull("Object must be valid", material2);
		Assert.assertTrue("Objects must be equal", material.equals(material2));
	}

	@Test
	public void testChildReferenceEquals() throws Throwable {
		ICacheFactory cacheFactory = beanContext.getService(ICacheFactory.class);
		ICache cache = cacheFactory.create(CacheFactoryDirective.SubscribeGlobalDCE, "test");

		Material material = cache.getObject(Material.class, 1);
		Material material2 = cache.getObject(Material.class, 1);

		Assert.assertTrue("References must be equal", material == material2);
	}

	@Test
	public void testMergeProxy() throws Throwable {
		IMergeService mergeService = beanContext.getService(IMergeService.class);

		CreateContainer insert1 = new CreateContainer();
		insert1.setReference(new DirectObjRef(Material.class, insert1));
		List<IPrimitiveUpdateItem> items1 = new ArrayList<>();
		List<IRelationUpdateItem> childItems1 = new ArrayList<>();
		PrimitiveUpdateItem pui = new PrimitiveUpdateItem();
		pui.setMemberName("Name");
		pui.setNewValue("Hallo");
		items1.add(pui);
		insert1.setPrimitives(ListUtil.toArray(IPrimitiveUpdateItem.class, items1));
		insert1.setRelations(ListUtil.toArray(IRelationUpdateItem.class, childItems1));

		CreateContainer insert2 = new CreateContainer();
		insert2.setReference(new DirectObjRef(Unit.class, insert2));
		List<IPrimitiveUpdateItem> items2 = new ArrayList<>();
		// List<IRelationUpdateItem> childItems2 = new
		// ArrayList<IRelationUpdateItem>();
		PrimitiveUpdateItem pui2 = new PrimitiveUpdateItem();
		pui2.setMemberName("Name");
		pui2.setNewValue("Unit Hallo");
		items2.add(pui2);
		insert2.setPrimitives(ListUtil.toArray(IPrimitiveUpdateItem.class, items2));
		// insert2.setChildItems(childItems2);

		RelationUpdateItem rui = new RelationUpdateItem();
		rui.setMemberName("Unit");
		rui.setAddedORIs(new IObjRef[] {insert2.getReference()});
		childItems1.add(rui);

		// RelationUpdateItem rui2 = new RelationUpdateItem();
		// rui2.setMemberName("Unit");
		// rui2.setAddedORIs(new IObjRef[] { insert1.getReference() });
		// childItems2.add(rui2);

		List<IChangeContainer> allChanges = new ArrayList<>();
		List<Object> originalRefs = new ArrayList<>();
		originalRefs.add(null);
		originalRefs.add(null);

		allChanges.add(insert1);
		allChanges.add(insert2);

		CUDResult cudResult = new CUDResult(allChanges, originalRefs);

		IOriCollection oriCollection = mergeService.merge(cudResult, null);

		List<IObjRef> allChangeORIs = oriCollection.getAllChangeORIs();
		Assert.assertTrue("Number of changes wrong", allChangeORIs.size() == 2);
		Assert.assertTrue("Primary key not assigned", allChangeORIs.get(0) != null);
		Assert.assertTrue("Primary key not assigned", allChangeORIs.get(1) != null);
	}

	@Test
	public void testEntityService() throws Throwable {
		IMaterialService materialService = beanContext.getService(IMaterialService.class);
		ICacheFactory cacheFactory = beanContext.getService(ICacheFactory.class, true);
		ICache cache = beanContext.getService(ICache.class);

		ICache childCache = cacheFactory.create(CacheFactoryDirective.SubscribeGlobalDCE, "test");

		List<Material> allMaterials = materialService.getAllMaterials();
		Assert.assertTrue("Materials count is 0", allMaterials.size() > 0);

		Material material = allMaterials.get(0);

		Material materialFromChildCache = childCache.getObject(Material.class, material.getId());

		// Version before change occurs
		Assert.assertEquals(material.getVersion(), materialFromChildCache.getVersion());

		Unit unit = material.getUnit();
		Assert.assertNotNull("Unit not valid", unit);
		Assert.assertEquals("cm", unit.getName());

		material.setName(material.getName() + "_Change");

		materialService.updateMaterial(material);

		Material materialFromCache = cache.getObject(Material.class, material.getId());

		Assert.assertEquals(material.getVersion(), materialFromCache.getVersion());
	}

	@Test
	public void testCreatedUpdated() throws Throwable {
		IMaterialService materialService = beanContext.getService(IMaterialService.class);
		ICache cache = beanContext.getService(ICache.class);

		Material material = entityFactory.createEntity(Material.class);
		material.setName("testCreatedUpdated");
		material.setMaterialGroup(cache.getObject(MaterialGroup.class, "pl"));
		material.setUnit(cache.getObject(Unit.class, 1));

		assertNull(material.getCreatedOn());
		assertNull(material.getCreatedBy());
		assertNull(material.getUpdatedOn());
		assertNull(material.getUpdatedBy());

		materialService.updateMaterial(material);

		assertNotNull(material.getCreatedOn());
		assertNotNull(material.getCreatedBy());
		assertFalse(material.getCreatedBy().isEmpty());
		assertNull(material.getUpdatedOn());
		assertNull(material.getUpdatedBy());

		material.setName("testCreatedUpdated 2.0");
		materialService.updateMaterial(material);

		assertNotNull(material.getCreatedOn());
		assertNotNull(material.getCreatedBy());
		assertFalse(material.getCreatedBy().isEmpty());
		assertNotNull(material.getUpdatedOn());
		assertNotNull(material.getUpdatedBy());
		assertFalse(material.getUpdatedBy().isEmpty());
	}

	@Test
	public void testArraySave() throws Throwable {
		IMaterialService materialService = beanContext.getService(IMaterialService.class);
		Material[] toSave = {ObjectMother.getNewMaterial(entityFactory, null, null, "M1"),
				ObjectMother.getNewMaterial(entityFactory, null, null, "M2")};

		materialService.updateMaterials(toSave);

		Material actual;
		actual = materialService.getMaterialByName("M1");
		assertNotNull(actual);
		assertNotNull(actual.getId());

		actual = materialService.getMaterialByName("M2");
		assertNotNull(actual);
		assertNotNull(actual.getId());
	}

	@Test
	public void testToOneRelationChange() throws Throwable {
		IMaterialService materialService = beanContext.getService(IMaterialService.class);
		ICache cache = beanContext.getService(ICache.class);

		List<Material> allMaterials = materialService.getAllMaterials();

		Assert.assertTrue("Materials count is 0", allMaterials.size() > 0);

		Material material = allMaterials.get(0);

		material.setUnit(null);

		Material materialFromCache = cache.getObject(Material.class, material.getId());
		// Version from cache must be in sync with version of current changed
		// object
		Assert.assertEquals(material.getVersion(), materialFromCache.getVersion());

		materialService.updateMaterial(material);

		materialFromCache = cache.getObject(Material.class, material.getId());
		// Version from cache must be in sync with version of current changed
		// object
		Assert.assertEquals(material.getVersion(), materialFromCache.getVersion());
		Assert.assertNull("Unit is not null!", materialFromCache.getUnit());
	}

	@Test
	@Ignore
	public void testToOneRelationUpdate() throws Throwable {
		IMaterialService materialService = beanContext.getService(IMaterialService.class);
		ICache cache = beanContext.getService(ICache.class);

		Material material = cache.getObject(Material.class, 1);

		// Whether 1 or 2, I take the other one
		Unit otherUnit =
				cache.getObject(Unit.class, ((Number) material.getUnit().getId()).longValue() % 2 + 1);

		material.setUnit(otherUnit);
		materialService.updateMaterial(material);

		Material materialFromCache = cache.getObject(Material.class, material.getId());
		Unit unitFromCache = materialFromCache.getUnit();
		Assert.assertEquals(material.getVersion(), materialFromCache.getVersion());
		Assert.assertTrue("Wrong unit!", ((Long) otherUnit.getId()).equals(unitFromCache.getId()));
		Assert.assertTrue("Wrong version!",
				((Long) otherUnit.getVersion()).equals(unitFromCache.getVersion()));
	}

	@Test
	public void testCreationOfNewMaterial() {
		ICache cache = beanContext.getService(ICache.class);
		IMaterialService materialService = beanContext.getService(IMaterialService.class);

		Material material = entityFactory.createEntity(Material.class);
		MaterialGroup mg = cache.getObject(MaterialGroup.class, "pl");
		Unit unit = cache.getObject(Unit.class, (long) 1);
		assertNotNull(mg);
		assertNotNull(unit);

		material.setName("new material");
		material.setMaterialGroup(mg);
		material.setUnit(unit);
		materialService.updateMaterial(material);
		assertNotNull("ID is still null!", material.getId());
		assertEquals("Wrong version!", 1, material.getVersion());

		Material actual = cache.getObject(Material.class, material.getId());
		assertNotNull("Group is null!", actual.getMaterialGroup());
		assertEquals("Wrong group!", mg.getId(), actual.getMaterialGroup().getId());
		assertNotNull("Unit is null!", actual.getUnit());
		assertEquals("Wrong unit!", unit.getId(), actual.getUnit().getId());
	}

	@Test
	public void testCreationOfNewMaterialWithNewUnit() {
		ICache cache = beanContext.getService(ICache.class);
		IMaterialService materialService = beanContext.getService(IMaterialService.class);

		Material material = entityFactory.createEntity(Material.class);
		MaterialGroup mg = cache.getObject(MaterialGroup.class, "pl");
		Unit unit = entityFactory.createEntity(Unit.class);
		assertNotNull(mg);

		unit.setName("new unit");

		material.setName("new material");
		material.setMaterialGroup(mg);
		material.setUnit(unit);

		materialService.updateMaterial(material);
		assertNotNull("Unit ID is still null!", material.getUnit().getId());
		assertEquals("Wrong Unit version!", 1, material.getUnit().getVersion());
	}

	@Test
	public void testCreationOfNewMaterialWithoutUnit() {
		Material material = entityFactory.createEntity(Material.class);
		MaterialGroup mg = cache.getObject(MaterialGroup.class, "pl");
		assertNotNull(mg);

		material.setName("new material");
		material.setMaterialGroup(mg);

		materialService.updateMaterial(material);
		assertNotNull("ID is still null!", material.getId());
		assertEquals("Wrong version!", 1, material.getVersion());

		Material actual = cache.getObject(Material.class, material.getId());
		assertEquals("Wrong group!", mg.getId(), actual.getMaterialGroup().getId());
		assertNull("Unit should be null!", actual.getUnit());
	}

	@Test
	public void testStingAsPrimaryKey() {
		ICache cache = beanContext.getService(ICache.class);

		MaterialGroup group = cache.getObject(MaterialGroup.class, "me");
		assertNotNull(group);
		assertEquals("me", group.getId());
		assertEquals("Metal", group.getName());
	}

	@Test
	public void testCreationOfOneToManyRelationWithoutLinkTable() {
		ICache cache = beanContext.getService(ICache.class);
		IMaterialService materialService = beanContext.getService(IMaterialService.class);

		Material material = cache.getObject(Material.class, 1);
		material.setMaterialGroup(null);
		materialService.updateMaterial(material);
		material = cache.getObject(Material.class, 1);
		assertNull(material.getMaterialGroup());

		MaterialGroup pl = cache.getObject(MaterialGroup.class, "pl");
		material.setMaterialGroup(pl);
		materialService.updateMaterial(material);
		material = cache.getObject(Material.class, 1);
		assertEquals(pl.getId(), material.getMaterialGroup().getId());
	}

	@Test
	public void testRetrieveWithTechnicalMembers() {
		ICache cache = beanContext.getService(ICache.class);

		Material material = cache.getObject(Material.class, 1);
		assertEquals("anonymous", material.getCreatedBy());
		assertNotNull("Created on null", material.getCreatedOn());
	}

	@Test
	public void testRetrievingOfOneToManyRelationWithoutLinkTable() {
		ICache cache = beanContext.getService(ICache.class);

		Material material = cache.getObject(Material.class, 1);
		MaterialGroup group = material.getMaterialGroup();
		assertNotNull(group);
		assertEquals("me", group.getId());
		assertEquals("Metal", group.getName());
	}

	@Test
	public void testUpdateOfOneToManyRelationWithoutLinkTable() {
		ICache cache = beanContext.getService(ICache.class);
		IMaterialService materialService = beanContext.getService(IMaterialService.class);

		Material material = cache.getObject(Material.class, 1);
		int version = material.getVersion();
		assertEquals("me", material.getMaterialGroup().getId());
		MaterialGroup group = cache.getObject(MaterialGroup.class, "pl");
		material.setMaterialGroup(group);

		materialService.updateMaterial(material);
		assertEquals(version + 1, material.getVersion());

		Material cachedMaterial = cache.getObject(Material.class, 1);
		assertEquals(material.getVersion(), cachedMaterial.getVersion());
		assertEquals("pl", cachedMaterial.getMaterialGroup().getId());
	}

	@Test
	public void testDeletionOfOneToManyRelationWithoutLinkTable() {
		ICache cache = beanContext.getService(ICache.class);
		IMaterialService materialService = beanContext.getService(IMaterialService.class);

		Material material = cache.getObject(Material.class, 1);
		assertNotNull(material.getMaterialGroup());
		material.setMaterialGroup(null);

		materialService.updateMaterial(material);

		Material material2 = cache.getObject(Material.class, 1);
		assertNull(material2.getMaterialGroup());
	}
}
