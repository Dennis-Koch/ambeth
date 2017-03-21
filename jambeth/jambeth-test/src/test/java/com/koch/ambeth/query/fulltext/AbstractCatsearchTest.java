package com.koch.ambeth.query.fulltext;

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

import java.util.List;

import org.junit.After;
import org.junit.Before;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.OrderByType;
import com.koch.ambeth.query.QueryEntity;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.collections.HashMap;

@SQLData("/com/koch/ambeth/query/Query_data.sql")
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile,
				value = "com/koch/ambeth/query/Query_orm.xml"),
		@TestProperties(name = "abc", value = "hallo"),
		@TestProperties(name = "abc", value = "hallo2")})
public abstract class AbstractCatsearchTest extends AbstractInformationBusWithPersistenceTest {
	protected static final String paramName1 = "param.1";
	protected static final String paramName2 = "param.2";
	protected static final String columnName1 = "ID";
	protected static final String propertyName1 = "Id";
	protected static final String columnName2 = "VERSION";
	protected static final String propertyName2 = "Version";
	protected static final String columnName3 = "FK";
	protected static final String propertyName3 = "Fk";
	protected static final String columnName4 = "CONTENT";
	protected static final String propertyName4 = "Content";
	protected static final String propertyName5 = "Name1";

	@Autowired
	protected IQueryBuilderFactory qbf;

	protected IQueryBuilder<QueryEntity> qb;

	protected HashMap<Object, Object> nameToValueMap = new HashMap<Object, Object>();

	@Property(name = "abc", defaultValue = "abc0")
	protected String value;

	@Before
	public void setUp() throws Exception {
		qb = qbf.create(QueryEntity.class);
		nameToValueMap.clear();
	}

	@After
	public void tearDown() throws Exception {
		qb = null;
	}

	@SuppressWarnings("deprecation")
	protected void fulltextDefault() throws Exception {
		IOperand rootOperand = qb.fulltext(qb.valueName(paramName1));
		qb.orderBy(qb.property("Id"), OrderByType.ASC);
		IQuery<QueryEntity> query = qb.build(rootOperand);

		HashMap<Object, Object> nameToValueMap = new HashMap<Object, Object>();
		nameToValueMap.put(paramName1, "me3");
		List<QueryEntity> result = query.retrieve(nameToValueMap);
		assertEquals(3, result.size());
		assertEquals(1, result.get(0).getId());
		assertEquals(3, result.get(1).getId());
		assertEquals(4, result.get(2).getId());
	}

}
