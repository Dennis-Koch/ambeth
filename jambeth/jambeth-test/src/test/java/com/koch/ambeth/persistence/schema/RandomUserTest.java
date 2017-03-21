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

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.oracle.RandomUserScript;
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
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

/**
 * Tests the replacement of the JAMBETH user by a temporary user created by the RandomUserScript. Because this is also done by the Jenkins build script this
 * test won't run in the Jenkins environment. Therefore the test methods are disabled.
 * <p>
 * This test will test the replacement of multiple schema place holders in ORM, query builder and test data insert scripts .
 */
@TestModule({ MultiSchemaTestModule.class })
@TestPropertiesList({ @TestProperties(file = "random_user_test.properties"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseBehaviourStrict, value = "true"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/schema/random_user_orm.xml") })
@SQLStructure("random_user_structure.sql")
@SQLData("random_user_data.sql")
@Ignore("Does not work in JENKINS")
public class RandomUserTest extends AbstractInformationBusWithPersistenceTest
{
	private static final String RANDOM_USER_TEST_PROPERTIES = "random_user_test.properties";

	@Autowired
	private ICache cache;

	@Autowired
	private IQueryBuilderFactory qbf;

	@Autowired
	private IParentAService parentAService;

	@Autowired
	private IParentBService parentBService;

	@BeforeClass
	public static void beforeClass()
	{
		String[] args = new String[] { RandomUserScript.SCRIPT_IS_CREATE + "=true", RandomUserScript.SCRIPT_USER_PASS + "=pw1,pw2",
				RandomUserScript.SCRIPT_USER_PROPERTYFILE + "=" + RANDOM_USER_TEST_PROPERTIES };
		runRandomUserScript(args);
	}

	@AfterClass
	public static void afterClass()
	{
		String[] args = new String[] { RandomUserScript.SCRIPT_IS_CREATE + "=false",
				RandomUserScript.SCRIPT_USER_PROPERTYFILE + "=" + RANDOM_USER_TEST_PROPERTIES };
		runRandomUserScript(args);
	}

	private static void runRandomUserScript(String[] args)
	{
		try
		{
			RandomUserScript.main(args);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Test
	public void testDeleteA() throws Exception
	{
		ParentA parent = cache.getObject(ParentA.class, 1);
		assertNotNull(parent);

		parentAService.delete(parent);

		ParentA actual = cache.getObject(ParentA.class, 1);
		assertNull(actual);
	}

	@Test
	public void testDeleteB() throws Exception
	{
		ParentB parent = cache.getObject(ParentB.class, 101);
		assertNotNull(parent);

		parentBService.delete(parent);

		ParentB actual = cache.getObject(ParentB.class, 101);
		assertNull(actual);
	}

	@Test
	public void testQueryA() throws Exception
	{
		IQueryBuilder<ParentA> qb = qbf.create(ParentA.class);
		IQuery<ParentA> query = qb.build(qb.isEqualTo(qb.property("Child.Id"), qb.value(11)));
		List<ParentA> result = query.retrieve();
		assertEquals(1, result.size());
		ParentA actual = result.get(0);
		assertEquals(1, actual.getId());
		assertEquals(11, actual.getChild().getId());
	}

	@Test
	public void testQueryB() throws Exception
	{
		IQueryBuilder<ParentB> qb = qbf.create(ParentB.class);
		IQuery<ParentB> query = qb.build(qb.isEqualTo(qb.property("Child.Id"), qb.value(111)));
		List<ParentB> result = query.retrieve();
		assertEquals(1, result.size());
		ParentB actual = result.get(0);
		assertEquals(101, actual.getId());
		assertEquals(111, actual.getChild().getId());
	}

}
