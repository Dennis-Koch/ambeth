package com.koch.ambeth.persistence.jdbc.bigstatements;

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

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.model.MaterialGroup;
import com.koch.ambeth.model.Unit;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLDataRebuild;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.category.SpecialTests;
import com.koch.ambeth.util.collections.ArrayList;

@Category(SpecialTests.class)
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseHost, value = "localhost"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseName, value = "jambeth"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseUser, value = "jambeth"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabasePass, value = "jambeth"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName,
				value = "jambeth"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseProtocol,
				value = "jdbc:postgresql"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabasePort, value = "1531"),
		@TestProperties(name = "ambeth.log.level.com.koch.ambeth.persistence.jdbc.JdbcTable",
				value = "DEBUG")})
@SQLStructure("BigStatement_structure.sql")
@SQLData("MaxParameters_data.sql")
@SQLDataRebuild(true)
public class StackOverflowRootCacheTest extends AbstractInformationBusWithPersistenceTest {

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IMergeProcess mergeProcess;

	/**
	 * This test is necessary to test the "maximal parameter" limitation of postgresql (it should work
	 * in any other database back end as well)
	 *
	 * @throws Exception
	 */
	@Test
	public void testStackOverflowRootCache() throws Exception {
		Material material;
		MaterialGroup materialGroup;
		Unit unit;
		ArrayList<Object> bigList = new ArrayList<>();
		for (int a = 400000; a-- > 0;) {
			material = entityFactory.createEntity(Material.class);
			material.setName("hugo");

			unit = entityFactory.createEntity(Unit.class);
			unit.setName("hugo2");

			materialGroup = entityFactory.createEntity(MaterialGroup.class);
			materialGroup.setName("asdf");

			material.setMaterialGroup(materialGroup);
			material.setUnit(unit);
			bigList.add(material);
		}
		mergeProcess.process(bigList, null, null, null, true);

	}
}
