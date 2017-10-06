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

import java.net.SocketException;
import java.sql.SQLRecoverableException;
import java.sql.SQLSyntaxErrorException;

import javax.persistence.PersistenceException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.persistence.api.ICursor;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.persistence.api.database.DatabaseCallback;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLDataRebuild;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.category.SlowTests;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.MaskingRuntimeException;

@Category(SlowTests.class)
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = "ambeth.log.level.com.koch.ambeth.persistence.jdbc.JdbcTable",
				value = "INFO")})
@SQLStructure("BigStatement_structure.sql")
@SQLData("BigStatement_data.sql")
@SQLDataRebuild(false)
public class BigStatementTest extends AbstractInformationBusWithPersistenceTest {
	@Test
	public void testBigQuery100000() throws Exception {
		String paramName = "paramName";
		IQueryBuilder<Material> qb = queryBuilderFactory.create(Material.class);
		IQuery<Material> query = qb.build(qb.let(qb.property("Id")).isIn(qb.valueName(paramName)));

		ArrayList<Object> bigList = new ArrayList<>();
		for (int a = 100000; a-- > 0;) {
			bigList.add(Integer.valueOf(a + 1));
		}
		try {
			IList<Material> materials = query.param(paramName, bigList).retrieve();
			Assert.assertNotNull(materials);
			Assert.assertEquals(90006, materials.size());
		}
		catch (MaskingRuntimeException e) {
			Throwable cause = e.getCause();
			Assert.assertTrue(cause instanceof PersistenceException);
			cause = cause.getCause();
			Assert.assertTrue(cause instanceof SQLSyntaxErrorException);
			Assert.assertEquals("ORA-01745: invalid host/bind variable name\n", cause.getMessage());
			throw e;
		}

	}

	@Test
	public void testBigQuery20000() throws Exception {
		String paramName = "paramName";
		IQueryBuilder<Material> qb = queryBuilderFactory.create(Material.class);
		IQuery<Material> query = qb.build(qb.let(qb.property("Id")).isIn(qb.valueName(paramName)));

		ArrayList<Object> bigList = new ArrayList<>();
		for (int a = 20000; a-- > 0;) {
			bigList.add(Integer.valueOf(a + 1));
		}
		try {
			IList<Material> materials = query.param(paramName, bigList).retrieve();
			Assert.assertNotNull(materials);
			Assert.assertEquals(10006, materials.size());
		}
		catch (MaskingRuntimeException e) {
			Throwable cause = e.getCause();
			Assert.assertTrue(cause instanceof PersistenceException);
			cause = cause.getCause();
			Assert.assertTrue(cause instanceof SQLRecoverableException);
			cause = cause.getCause();
			Assert.assertTrue(cause instanceof SocketException);
			Assert.assertEquals("Connection reset by peer: socket write error", cause.getMessage());
			throw e;
		}
	}

	@Test
	public void testSelectFields100000() throws Exception {
		final ArrayList<Object> bigList = new ArrayList<>();
		for (int a = 100001; a-- > 0;) {
			bigList.add(Integer.valueOf(a + 1));
		}
		transaction.processAndCommit(new DatabaseCallback() {
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
					throws Exception {
				IDatabase database = persistenceUnitToDatabaseMap.iterator().next().getValue();
				ITable table = database.getTableByType(Material.class);
				IVersionCursor cursor = table.selectVersion(bigList);
				try {

				}
				finally {
					cursor.dispose();
				}
			}
		});
		transaction.processAndCommit(new DatabaseCallback() {
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
					throws Exception {
				IDatabase database = persistenceUnitToDatabaseMap.iterator().next().getValue();
				ITable table = database.getTableByType(Material.class);
				ICursor cursor = table.selectValues(bigList);
				try {

				}
				finally {
					cursor.dispose();
				}
			}
		});
	}

	@Test
	public void testMerge100000() throws Exception {
		IQueryBuilder<Material> qb = queryBuilderFactory.create(Material.class);
		IQuery<Material> query = qb.build(qb.all());
		IList<Material> materials = query.retrieve();
		Assert.assertTrue(materials.size() > 100000);

		for (Material material : materials) {
			material.setName(material.getName() + "2");
		}
		beanContext.getService(IMergeProcess.class).process(materials);
	}
}
