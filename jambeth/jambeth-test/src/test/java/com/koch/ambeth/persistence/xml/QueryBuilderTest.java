package com.koch.ambeth.persistence.xml;

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

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.persistence.xml.model.Employee;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.collections.IList;

@SQLData("/com/koch/ambeth/persistence/xml/Relations_data.sql")
@SQLStructure("/com/koch/ambeth/persistence/xml/Relations_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/persistence/xml/orm.xml")
@TestModule(TestServicesModule.class)
public class QueryBuilderTest extends AbstractInformationBusWithPersistenceTest {
	@Test
	public void testComplexQuery() {
		IQueryBuilder<Employee> queryBuilder = queryBuilderFactory.create(Employee.class);
		String paramName = "myParam";

		IQuery<Employee> query = queryBuilder.build(queryBuilder.let(
				queryBuilder.property("PrimaryProject.Employees.OtherAddresses.Street")).isEqualTo(
						queryBuilder.valueName(paramName), Boolean.FALSE));
		IList<Employee> employees = query.param(paramName, "TestStreet").retrieve();
		Assert.assertNotNull(employees);
	}
}
