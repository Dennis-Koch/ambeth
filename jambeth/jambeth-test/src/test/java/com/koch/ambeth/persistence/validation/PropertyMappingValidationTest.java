package com.koch.ambeth.persistence.validation;

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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.TestRebuildContext;
import com.koch.ambeth.testutil.category.ReminderTests;

@TestRebuildContext
@SQLStructure("PropertyMappingValidation_structure.sql")
public class PropertyMappingValidationTest extends AbstractInformationBusWithPersistenceTest {
	@Test
	@TestProperties(name = ServiceConfigurationConstants.mappingFile,
			value = "com/koch/ambeth/persistence/validation/orm_ok_minimal.xml")
	public void test_orm_ok() {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Entity.class);
		assertNotNull(metaData);

		assertNotNull(metaData.getMemberByName("Name"));
		assertNull(metaData.getMemberByName("NoDb"));
	}

	@Test
	@TestProperties(name = ServiceConfigurationConstants.mappingFile,
			value = "com/koch/ambeth/persistence/validation/orm_ok_expliciteProperty.xml")
	public void test_orm_ok_expliciteProperty() {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Entity.class);
		assertNotNull(metaData);

		assertNotNull(metaData.getMemberByName("Name"));
		assertNull(metaData.getMemberByName("NoDb"));
	}

	@Category(ReminderTests.class)
	// FIXME The NPE is thrown during test context setup and not during the test.
	@Test(expected = NullPointerException.class)
	@TestProperties(name = ServiceConfigurationConstants.mappingFile,
			value = "com/koch/ambeth/persistence/validation/orm_fail_noBackingDbField.xml")
	public void test_orm_fail_noBackingDbField() {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Entity.class);
		assertNotNull(metaData);

		assertNotNull(metaData.getMemberByName("Name"));
		assertNull(metaData.getMemberByName("NoDb"));
	}

	@Test
	@TestPropertiesList({
			@TestProperties(name = ServiceConfigurationConstants.mappingFile,
					value = "com/koch/ambeth/persistence/validation/orm_ok_minimal.xml"),
			@TestProperties(name = ServiceConfigurationConstants.valueObjectFile,
					value = "com/koch/ambeth/persistence/validation/vo_ok_minimal.xml"),
			@TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true"),
			@TestProperties(name = MergeConfigurationConstants.ValueObjectConfigValidationActive,
					value = "true")})
	public void test_vo_ok() {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Entity.class);
		assertNotNull(metaData);

		IValueObjectConfig valueObjectConfig =
				entityMetaDataProvider.getValueObjectConfig(EntityVO.class);
		assertNotNull(valueObjectConfig);
	}

	@Test
	@TestPropertiesList({
			@TestProperties(name = ServiceConfigurationConstants.mappingFile,
					value = "com/koch/ambeth/persistence/validation/orm_ok_minimal.xml"),
			@TestProperties(name = ServiceConfigurationConstants.valueObjectFile,
					value = "com/koch/ambeth/persistence/validation/vo_ok_expliciteMapping.xml"),
			@TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true"),
			@TestProperties(name = MergeConfigurationConstants.ValueObjectConfigValidationActive,
					value = "true")})
	public void test_vo_ok_expliciteMapping() {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Entity.class);
		assertNotNull(metaData);

		IValueObjectConfig valueObjectConfig =
				entityMetaDataProvider.getValueObjectConfig(EntityVO.class);
		assertNotNull(valueObjectConfig);
	}

	@Category(ReminderTests.class)
	// FIXME The IllegalStateException is thrown during test context setup and not during the test.
	@Test(expected = IllegalStateException.class)
	@TestPropertiesList({
			@TestProperties(name = ServiceConfigurationConstants.mappingFile,
					value = "com/koch/ambeth/persistence/validation/orm_ok_minimal.xml"),
			@TestProperties(name = ServiceConfigurationConstants.valueObjectFile,
					value = "com/koch/ambeth/persistence/validation/vo_fail_noBackingDbField.xml"),
			@TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true"),
			@TestProperties(name = MergeConfigurationConstants.ValueObjectConfigValidationActive,
					value = "true")})
	public void test_vo_fail_noBackingDbField() {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Entity.class);
		assertNotNull(metaData);
	}
}
