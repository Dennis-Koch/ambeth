package de.osthus.ambeth.persistence.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.persistence.xml.model.Employee;
import de.osthus.ambeth.persistence.xml.model.EmployeeSmallType;
import de.osthus.ambeth.persistence.xml.model.EmployeeType;
import de.osthus.ambeth.persistence.xml.model.ProjectType;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

@SQLData("Relations_data.sql")
@SQLStructure("Relations_structure.sql")
@TestModule(TestServicesModule.class)
@TestPropertiesList({ @TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/xml/orm.xml"),
		@TestProperties(name = ConfigurationConstants.valueObjectFile, value = "de/osthus/ambeth/persistence/xml/value-object.xml"),
		@TestProperties(name = ConfigurationConstants.GenericTransferMapping, value = "true") })
public class ValueObjectTest extends AbstractPersistenceTest
{
	@Test
	public void testGetValueObjectConfig() throws Throwable
	{
		IEntityMetaDataProvider entityMetaDataProvider = beanContext.getService(IEntityMetaDataProvider.class);
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
	public void testIgnoredMembers() throws Throwable
	{
		IEntityMetaDataProvider entityMetaDataProvider = beanContext.getService(IEntityMetaDataProvider.class);
		IValueObjectConfig actual = entityMetaDataProvider.getValueObjectConfig(ProjectType.class);
		assertTrue(actual.isIgnoredMember("IgnoreMe"));
	}
}