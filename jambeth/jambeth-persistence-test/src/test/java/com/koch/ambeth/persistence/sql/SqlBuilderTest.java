package com.koch.ambeth.persistence.sql;

/*-
 * #%L
 * jambeth-persistence-test
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
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.koch.ambeth.persistence.PersistenceHelper;
import com.koch.ambeth.persistence.sql.SqlBuilder;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.objectcollector.NoOpObjectCollector;

public class SqlBuilderTest {
	private static final int batchSize = 3;

	private SqlBuilder fixture;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		NoOpObjectCollector oc = new NoOpObjectCollector();
		fixture = new SqlBuilder();

		PersistenceHelper persistenceHelper = new PersistenceHelper();
		ReflectUtil.getDeclaredField(persistenceHelper.getClass(), "batchSize").set(persistenceHelper,
				batchSize);
		ReflectUtil.getDeclaredField(persistenceHelper.getClass(), "objectCollector")
				.set(persistenceHelper, oc);
		fixture.setObjectCollector(oc);
		fixture.setPersistenceHelper(persistenceHelper);

		fixture.afterPropertiesSet();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAfterPropertiesSet() {
		fixture.afterPropertiesSet();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAfterPropertiesSet_noPersistenceHelper() {
		fixture.persistenceHelper = null;
		fixture.afterPropertiesSet();
	}

	@Test
	public void testSetPersistenceHelper() {
		assertNotNull(fixture.persistenceHelper);
		fixture.setPersistenceHelper(null);
		assertNull(fixture.persistenceHelper);
	}

	@Test
	@Ignore
	public void testAppendNameValue() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testAppendNameValues() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testAppendName() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testAppendValue() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testIsUnescapedType() {
		fail("Not yet implemented"); // TODO
	}
}
