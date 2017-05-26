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

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLDataRebuild;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.category.SpecialTests;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;

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
// @SQLData("MaxParameters_data.sql")
@SQLDataRebuild(true)
public class MaxParametersInPreparedStatementTest
		extends AbstractInformationBusWithPersistenceTest {

	@Autowired
	protected ICache cache;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	/**
	 * This test is necessary to test the "maximal parameter" limitation of postgresql (it should work
	 * in any other database back end as well)
	 *
	 * @throws Exception
	 */
	@Test
	public void test40000ParemetersInPreparedStatementTest() throws Exception {
		String paramName = "paramName";
		IQueryBuilder<Material> qb = queryBuilderFactory.create(Material.class);
		IQuery<Material> query = qb.build(qb.isIn(qb.property("Id"), qb.valueName(paramName)));

		ArrayList<Object> bigList = new ArrayList<>();
		for (int a = 40000; a-- > 0;) {
			bigList.add(Integer.valueOf(a + 1));
		}
		// this getObjects call leads Ambeth to create a query with 40000 parameters, this is more then
		// the expected limit in postgres (32.000...) if this call
		// does not throw an exception, it works (no further assertions needed)
		cache.getObjects(Material.class, bigList);
	}

	@Test
	public void testInsertAndRetrive4500() throws Exception {

		ArrayList<Material> materials = new ArrayList<>();

		for (int a = 4500; a-- > 0;) {
			Material material = entityFactory.createEntity(Material.class);
			material.setName("" + a);
			materials.add(material);
		}

		// insert
		beanContext.getService(IMergeProcess.class).process(materials, null, null, null);

		IQueryBuilder<Material> qb = queryBuilderFactory.create(Material.class);
		IQuery<Material> query = qb.build(qb.all());
		IList<Material> materialsFromDB = query.retrieve();
		// Retrieve
		Assert.assertTrue(materialsFromDB.size() == 4500);

		// clear cache
		eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

		// load all objects
		String paramName = "paramName";
		qb = queryBuilderFactory.create(Material.class);
		query = qb.build(qb.isIn(qb.property("Name"), qb.valueName(paramName)));

		ArrayList<Object> bigList = new ArrayList<>();
		for (int a = 4500; a-- > 0;) {
			bigList.add(Integer.valueOf(a + 1));
		}
		cache.getObjects(Material.class, bigList);
	}
}
