package de.osthus.ambeth.persistence.validation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.testutil.TestRebuildContext;
import de.osthus.ambeth.testutil.category.ReminderTests;

@TestRebuildContext
@SQLStructure("PropertyMappingValidation_structure.sql")
public class PropertyMappingValidationTest extends AbstractPersistenceTest
{
	@Test
	@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/validation/orm_ok_minimal.xml")
	public void test_orm_ok()
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Entity.class);
		assertNotNull(metaData);

		assertNotNull(metaData.getMemberByName("Name"));
		assertNull(metaData.getMemberByName("NoDb"));
	}

	@Test
	@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/validation/orm_ok_expliciteProperty.xml")
	public void test_orm_ok_expliciteProperty()
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Entity.class);
		assertNotNull(metaData);

		assertNotNull(metaData.getMemberByName("Name"));
		assertNull(metaData.getMemberByName("NoDb"));
	}

	@Category(ReminderTests.class)
	// FIXME The NPE is thrown during test context setup and not during the test.
	@Test(expected = NullPointerException.class)
	@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/validation/orm_fail_noBackingDbField.xml")
	public void test_orm_fail_noBackingDbField()
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Entity.class);
		assertNotNull(metaData);

		assertNotNull(metaData.getMemberByName("Name"));
		assertNull(metaData.getMemberByName("NoDb"));
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/validation/orm_ok_minimal.xml"),
			@TestProperties(name = ConfigurationConstants.valueObjectFile, value = "de/osthus/ambeth/persistence/validation/vo_ok_minimal.xml"),
			@TestProperties(name = ConfigurationConstants.GenericTransferMapping, value = "true"),
			@TestProperties(name = MergeConfigurationConstants.ValueObjectConfigValidationActive, value = "true") })
	public void test_vo_ok()
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Entity.class);
		assertNotNull(metaData);

		IValueObjectConfig valueObjectConfig = entityMetaDataProvider.getValueObjectConfig(EntityVO.class);
		assertNotNull(valueObjectConfig);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/validation/orm_ok_minimal.xml"),
			@TestProperties(name = ConfigurationConstants.valueObjectFile, value = "de/osthus/ambeth/persistence/validation/vo_ok_expliciteMapping.xml"),
			@TestProperties(name = ConfigurationConstants.GenericTransferMapping, value = "true"),
			@TestProperties(name = MergeConfigurationConstants.ValueObjectConfigValidationActive, value = "true") })
	public void test_vo_ok_expliciteMapping()
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Entity.class);
		assertNotNull(metaData);

		IValueObjectConfig valueObjectConfig = entityMetaDataProvider.getValueObjectConfig(EntityVO.class);
		assertNotNull(valueObjectConfig);
	}

	@Category(ReminderTests.class)
	// FIXME The IllegalStateException is thrown during test context setup and not during the test.
	@Test(expected = IllegalStateException.class)
	@TestPropertiesList({ @TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/validation/orm_ok_minimal.xml"),
			@TestProperties(name = ConfigurationConstants.valueObjectFile, value = "de/osthus/ambeth/persistence/validation/vo_fail_noBackingDbField.xml"),
			@TestProperties(name = ConfigurationConstants.GenericTransferMapping, value = "true"),
			@TestProperties(name = MergeConfigurationConstants.ValueObjectConfigValidationActive, value = "true") })
	public void test_vo_fail_noBackingDbField()
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Entity.class);
		assertNotNull(metaData);
	}
}
