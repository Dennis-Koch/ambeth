package com.koch.ambeth.persistence.xml;

import java.util.Optional;

import org.junit.Assert;

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

import com.koch.ambeth.persistence.xml.model.Employee;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.collections.HashMap;

@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/persistence/xml/orm20.xml")
public class Relations20Test extends RelationsTest {
	@Test
	public void testHashMapProperty() {
		Employee employee = cache.getObject(Employee.class, 1);
		employee.setAttributes(new HashMap<>());
		employee.getAttributes().put("hallo", "welt");
		employeeService.save(employee);
	}

	@Test
	public void testOptionalProperty() {
		Employee newEmp = entityFactory.createEntity(Employee.class);
		Assert.assertNotNull(newEmp.getName2());
		Assert.assertFalse(newEmp.getName2().isPresent());

		IQueryBuilder<Employee> qb = queryBuilderFactory.create(Employee.class);
		Employee emp =
				qb.build(qb.isEqualTo(qb.plan().getName2(), qb.value("John Doe2"))).retrieveSingle();
		Assert.assertEquals("John Doe2", emp.getName2().get());

		emp.setName2(Optional.of("John Doe3"));
		employeeService.save(emp);

		Assert.assertEquals("John Doe3", emp.getName2().get());


	}
}
