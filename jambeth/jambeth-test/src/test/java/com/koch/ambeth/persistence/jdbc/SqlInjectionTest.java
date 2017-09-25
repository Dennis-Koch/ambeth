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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.cache.config.CacheConfigurationConstants;
import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.service.IMaterialService;
import com.koch.ambeth.service.TestServicesModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.IList;

@SQLData("Example_data.sql")
@SQLStructure("JDBCDatabase_structure.sql")
@TestModule(TestServicesModule.class)
@TestPropertiesList({
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml"),
		@TestProperties(name = CacheConfigurationConstants.FirstLevelCacheType, value = "PROTOTYPE")})
public class SqlInjectionTest extends AbstractInformationBusWithPersistenceTest {
	protected IMaterialService materialService;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(materialService, "materialService");
	}

	public void setMaterialService(IMaterialService materialService) {
		this.materialService = materialService;
	}

	@Test
	public void testInjectByMerge() {
		Material material = entityFactory.createEntity(Material.class);
		material.setName("Inject'; DROP TABLE MATERIAL;");
		materialService.updateMaterial(material);
		List<Material> allMaterials = materialService.getAllMaterials();
		Assert.assertTrue(material.getId() != 0);
		Assert.assertTrue(allMaterials.size() >= 1);
	}

	@Test
	public void testInjectByQuery2() {
		String name = "hallo";
		Material material = entityFactory.createEntity(Material.class);
		material.setName(name);
		materialService.updateMaterial(material);
		IQueryBuilder<Material> qb = queryBuilderFactory.create(Material.class);
		IQuery<Material> query = qb.build(qb.let(qb.property("Name")).isEqualTo(qb.value(name)));
		IList<Material> materials = query.retrieve();
		Assert.assertTrue(materials.size() >= 1);
	}

	@Test
	public void testInjectByQuery() {
		String name = "Inject'; DROP TABLE MATERIAL;";
		Material material = entityFactory.createEntity(Material.class);
		material.setName(name);
		materialService.updateMaterial(material);
		IQueryBuilder<Material> qb = queryBuilderFactory.create(Material.class);
		IQuery<Material> query = qb.build(qb.let(qb.property("Name")).isEqualTo(qb.value(name)));
		IList<Material> materials = query.retrieve();
		Assert.assertTrue(materials.size() >= 1);
	}
}
