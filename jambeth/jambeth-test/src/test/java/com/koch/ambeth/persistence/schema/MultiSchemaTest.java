package com.koch.ambeth.persistence.schema;

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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.schema.models.ChildA;
import com.koch.ambeth.persistence.schema.models.ChildB;
import com.koch.ambeth.persistence.schema.models.IParentAService;
import com.koch.ambeth.persistence.schema.models.IParentBService;
import com.koch.ambeth.persistence.schema.models.ParentA;
import com.koch.ambeth.persistence.schema.models.ParentB;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.category.ReminderTests;

/*
 * Granting rights to user in other schema: GRANT ALL ON <other schema name>.<table name> TO <user
 * name>; GRANT SELECT ON <other schema name>.<sequence name> TO <user name>;
 */
@Category(ReminderTests.class)
@TestModule({MultiSchemaTestModule.class})
@TestPropertiesList({
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName,
				value = "ci_admin:ci_admin_b"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseBehaviourStrict,
				value = "true"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile,
				value = "com/koch/ambeth/persistence/schema/orm.xml")})
@SQLStructure("structure.sql")
@SQLData("data.sql")
public class MultiSchemaTest extends AbstractInformationBusWithPersistenceTest {
	@Autowired
	private ICache cache;

	@Autowired
	private IQueryBuilderFactory qbf;

	@Autowired
	private IParentAService parentAService;

	@Autowired
	private IParentBService parentBService;

	@Test
	public void testCreateA() throws Exception {
		ChildA child = entityFactory.createEntity(ChildA.class);
		ParentA parent = entityFactory.createEntity(ParentA.class);
		parent.setChild(child);

		parentAService.create(parent);
	}

	@Test
	public void testCreateB() throws Exception {
		ChildB child = entityFactory.createEntity(ChildB.class);
		ParentB parent = entityFactory.createEntity(ParentB.class);
		parent.setChild(child);

		parentBService.create(parent);
	}

	@Test
	public void testRetrieveA() throws Exception {
		ParentA parent = parentAService.retrieve(1);
		assertNotNull(parent);
		assertEquals(1, parent.getId());
		assertEquals(1, parent.getVersion());
		ChildA child = parent.getChild();
		assertNotNull(child);
		assertEquals(11, child.getId());
		assertEquals(1, child.getVersion());
	}

	@Test
	public void testRetrieveB() throws Exception {
		ParentB parent = parentBService.retrieve(101);
		assertNotNull(parent);
		assertEquals(101, parent.getId());
		assertEquals(1, parent.getVersion());
		ChildB child = parent.getChild();
		assertNotNull(child);
		assertEquals(111, child.getId());
		assertEquals(1, child.getVersion());
	}

	@Test
	public void testUpdateA() throws Exception {
		ParentA parent = cache.getObject(ParentA.class, 1);
		ChildA child = parent.getChild();
		parent.setChild(entityFactory.createEntity(ChildA.class));
		parentAService.update(parent);

		ParentA actual = cache.getObject(ParentA.class, 1);
		assertEquals(2, actual.getVersion());
		assertTrue(child.getId() != actual.getChild().getId());
	}

	@Test
	public void testUpdateB() throws Exception {
		ParentB parent = cache.getObject(ParentB.class, 101);
		ChildB child = parent.getChild();
		parent.setChild(entityFactory.createEntity(ChildB.class));
		parentBService.update(parent);

		ParentB actual = cache.getObject(ParentB.class, 101);
		assertEquals(2, actual.getVersion());
		assertTrue(child.getId() != actual.getChild().getId());
	}

	@Test
	public void testDeleteA() throws Exception {
		ParentA parent = cache.getObject(ParentA.class, 1);
		assertNotNull(parent);

		parentAService.delete(parent);

		ParentA actual = cache.getObject(ParentA.class, 1);
		assertNull(actual);
	}

	@Test
	public void testDeleteB() throws Exception {
		ParentB parent = cache.getObject(ParentB.class, 101);
		assertNotNull(parent);

		parentBService.delete(parent);

		ParentB actual = cache.getObject(ParentB.class, 101);
		assertNull(actual);
	}

	@Test
	public void testQueryA() throws Exception {
		IQueryBuilder<ParentA> qb = qbf.create(ParentA.class);
		IQuery<ParentA> query = qb.build(qb.isEqualTo(qb.property("Child.Id"), qb.value(11)));
		List<ParentA> result = query.retrieve();
		assertEquals(1, result.size());
		ParentA actual = result.get(0);
		assertEquals(1, actual.getId());
		assertEquals(11, actual.getChild().getId());
	}

	@Test
	public void testQueryB() throws Exception {
		IQueryBuilder<ParentB> qb = qbf.create(ParentB.class);
		IQuery<ParentB> query = qb.build(qb.isEqualTo(qb.property("Child.Id"), qb.value(111)));
		List<ParentB> result = query.retrieve();
		assertEquals(1, result.size());
		ParentB actual = result.get(0);
		assertEquals(101, actual.getId());
		assertEquals(111, actual.getChild().getId());
	}

}
