package com.koch.ambeth.persistence.validation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLStructure;
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
