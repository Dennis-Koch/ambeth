package com.koch.ambeth.query;

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

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestProperties;

@SQLData("StoredFunction_data.sql")
@SQLStructure("StoredFunction_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/query/Query_orm.xml")
public class StoredFunctionTest extends AbstractInformationBusWithPersistenceTest
{
	protected static final String paramName1 = "param.1";
	protected static final String paramName2 = "param.2";
	protected static final String paramName3 = "param.3";
	protected static final String propertyName1 = "Id";
	protected static final String propertyName2 = "Version";

	protected IQueryBuilder<QueryEntity> qb;

	protected HashMap<Object, Object> nameToValueMap = new HashMap<Object, Object>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
		qb = queryBuilderFactory.create(QueryEntity.class);
	}

	@Override
	public void destroy() throws Throwable
	{
		super.destroy();
		nameToValueMap.clear();
	}

	@Test
	public void testFinalize() throws Exception
	{
		int value = 2;

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property(propertyName1), qb.function("getDoubled", qb.value(value))));

		List<QueryEntity> actual = query.retrieve();

		assertEquals(1, actual.size());
		assertEquals(value * 2, actual.get(0).getId());
	}

	@Test
	public void testMultipleParameters() throws Exception
	{
		int value = 2;

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(
				qb.property(propertyName1),
				qb.function("multiParams", qb.property(propertyName2), qb.value("BwQKSwe1RfgnSdDldsfnskQakDl0Q3CbHr2qwXSQin63x81MBm5ryiaE54ohMFSBTr"),
						qb.value(value))));

		List<QueryEntity> actual = query.retrieve();

		assertEquals(1, actual.size());
		assertEquals(value * 2, actual.get(0).getId());
	}

	@Test
	public void testFunctionFirst() throws Exception
	{
		int value = 2;

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(
				qb.function("multiParams", qb.property(propertyName2), qb.value("BwQKSwe1RfgnSdDldsfnskQakDl0Q3CbHr2qwXSQin63x81MBm5ryiaE54ohMFSBTr"),
						qb.value(value)), qb.value(4)));

		List<QueryEntity> actual = query.retrieve();

		assertEquals(5, actual.size()); // All entities
	}
}
