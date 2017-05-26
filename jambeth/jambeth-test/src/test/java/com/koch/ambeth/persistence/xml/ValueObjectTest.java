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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.persistence.xml.model.Employee;
import com.koch.ambeth.persistence.xml.model.EmployeeSmallType;
import com.koch.ambeth.persistence.xml.model.EmployeeType;
import com.koch.ambeth.persistence.xml.model.ProjectType;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;

@SQLData("Relations_data.sql")
@SQLStructure("Relations_structure.sql")
@TestModule(TestServicesModule.class)
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile,
				value = "com/koch/ambeth/persistence/xml/orm.xml"),
		@TestProperties(name = ServiceConfigurationConstants.valueObjectFile,
				value = "com/koch/ambeth/persistence/xml/value-object.xml"),
		@TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true")})
public class ValueObjectTest extends AbstractInformationBusWithPersistenceTest {
	@Test
	public void testGetValueObjectConfig() throws Throwable {
		IEntityMetaDataProvider entityMetaDataProvider =
				beanContext.getService(IEntityMetaDataProvider.class);
		IValueObjectConfig actual = entityMetaDataProvider.getValueObjectConfig(EmployeeType.class);
		assertEquals(EmployeeType.class, actual.getValueType());
		assertEquals(Employee.class, actual.getEntityType());
		IEntityMetaData metaData1 = entityMetaDataProvider.getMetaData(actual.getEntityType());
		assertNotNull(metaData1);

		actual = entityMetaDataProvider.getValueObjectConfig(EmployeeSmallType.class);
		assertEquals(EmployeeSmallType.class, actual.getValueType());
		assertEquals(Employee.class, actual.getEntityType());
		IEntityMetaData metaData2 = entityMetaDataProvider.getMetaData(actual.getEntityType());
		assertNotNull(metaData2);

		assertEquals(metaData1, metaData2);
	}

	@Test
	public void testIgnoredMembers() throws Throwable {
		IEntityMetaDataProvider entityMetaDataProvider =
				beanContext.getService(IEntityMetaDataProvider.class);
		IValueObjectConfig actual = entityMetaDataProvider.getValueObjectConfig(ProjectType.class);
		assertTrue(actual.isIgnoredMember("IgnoreMe"));
	}
}
