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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.category.SlowTests;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;

@Category(SlowTests.class)
@TestPropertiesList({@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/query/QueryMassdata_orm.xml")})
@SQLStructure("QueryMassdata_structure.sql")
@SQLData("Query10000_data.sql")
public class Query10000Test extends AbstractInformationBusWithPersistenceTest {
	protected static final String paramName1 = "param.1";

	protected IQueryBuilder<QueryEntity> qb;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		qb = queryBuilderFactory.create(QueryEntity.class);
	}

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "2000")
	public void testRetrieveFetch2000() throws Exception {
		IQuery<QueryEntity> query = qb.build(qb.like(qb.property("Name1"), qb.valueName(paramName1)));
		IList<QueryEntity> result = query.param(paramName1, "Q\\_%\\_Name%").retrieve();
		Assert.assertEquals(10000, result.size());
	}

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "1000")
	public void testRetrieveFetch1000() throws Exception {
		IQuery<QueryEntity> query = qb.build();
		IList<QueryEntity> result = query.retrieve();
		Assert.assertEquals(10004, result.size());
		List<String> names = new ArrayList<>(result.size());
		for (int a = result.size(); a-- > 0;) {
			names.add(result.get(a).getName1());
		}
		qb = queryBuilderFactory.create(QueryEntity.class);
		IQuery<QueryEntity> query2 = qb.build(qb.and(qb.isIn(qb.property("Name1"), qb.value(names)),
				qb.isEqualTo(qb.property("Version"), qb.value(3))));
		IList<QueryEntity> result2 = query2.retrieve();
		Assert.assertEquals(5000, result2.size());
	}

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "100")
	public void testRetrieveFetch100() throws Exception {
		IQuery<QueryEntity> query = qb.build();
		IList<QueryEntity> result = query.retrieve();
		Assert.assertEquals(10004, result.size());
	}

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "10")
	public void testRetrieveFetch10() throws Exception {
		IQuery<QueryEntity> query = qb.build();
		IList<QueryEntity> result = query.retrieve();
		Assert.assertEquals(10004, result.size());
	}

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "1")
	public void testRetrieveFetch1() throws Exception {
		IQuery<QueryEntity> query = qb.build();
		IList<QueryEntity> result = query.retrieve();
		Assert.assertEquals(10004, result.size());
	}
}
