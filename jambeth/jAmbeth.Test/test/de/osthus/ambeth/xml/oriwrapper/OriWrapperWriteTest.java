package de.osthus.ambeth.xml.oriwrapper;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.ioc.BootstrapScannerModule;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.service.ProcessServiceTestModule;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLDataList;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.SQLStructureList;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.xml.ICyclicXMLHandler;
import de.osthus.ambeth.xml.oriwrapper.OriWrapperTestBed.TestData;

@SQLStructureList({ @SQLStructure("../../service/ProcessServiceTest_structure.sql"), @SQLStructure("OriWrapper_structure.sql") })
@SQLDataList({ @SQLData("../../service/ProcessServiceTest_data.sql"), @SQLData("OriWrapper_data.sql") })
@TestPropertiesList({ @TestProperties(file = "de/osthus/ambeth/xml/oriwrapper/OriWrapperTestData.properties"),
		@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/service/orm.xml;de/osthus/ambeth/xml/oriwrapper/orm.xml") })
@TestModule({ BootstrapScannerModule.class, XmlModule.class, ProcessServiceTestModule.class, OriWrapperTestModule.class })
public class OriWrapperWriteTest extends AbstractPersistenceTest
{
	@Autowired
	protected ICyclicXMLHandler cyclicXmlHandler;

	@Autowired
	protected OriWrapperTestBed oriWrapperTestBed;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		oriWrapperTestBed.init();
	}

	@Test
	public void writeSimpleEntity()
	{
		TestData testData = oriWrapperTestBed.getSimpleEntityTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeEntityWithRelation()
	{
		TestData testData = oriWrapperTestBed.getEntityWithRelationTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeMixedArray()
	{
		TestData testData = oriWrapperTestBed.getMixedArrayTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeMixedList()
	{
		TestData testData = oriWrapperTestBed.getMixedListTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeMixedLinkedSet()
	{
		TestData testData = oriWrapperTestBed.getMixedLinkedSetTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeServiceDescription() throws SecurityException, NoSuchMethodException
	{
		TestData testData = oriWrapperTestBed.getServiceDescriptionTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeCreatedEntity()
	{
		TestData testData = oriWrapperTestBed.getCreatedEntityTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeCreatedChildEntity()
	{
		TestData testData = oriWrapperTestBed.getCreatedChildEntityTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		if (!testData.xml.equals(xml))
		{
			// Compensate for loss of order in set in CUDResut
			testData = oriWrapperTestBed.getCreatedChildEntityTestData2();
		}
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeCreatedParentAndChildEntities()
	{
		TestData testData = oriWrapperTestBed.getCreatedParentAndChildEntitiesTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		if (!testData.xml.equals(xml))
		{
			// Compensate for loss of order in set
			testData = oriWrapperTestBed.getCreatedParentAndChildEntitiesTestData2();
		}
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeCreatedParentAndChildEntitiesInList()
	{
		TestData testData = oriWrapperTestBed.getCreatedParentAndChildEntitiesInListTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		if (!testData.xml.equals(xml))
		{
			// Compensate for loss of order in set
			testData = oriWrapperTestBed.getCreatedParentAndChildEntitiesInListTestData2();
		}
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeMultipleCreatedEntities()
	{
		TestData testData = oriWrapperTestBed.getMultipleCreatedEntitiesTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		if (!testData.xml.equals(xml))
		{
			// Compensate for loss of order in set in CUDResut
			testData = oriWrapperTestBed.getMultipleCreatedEntitiesTestData2();
		}
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeUpdatedEntity()
	{
		TestData testData = oriWrapperTestBed.getUpdatedEntityTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeBuidUpdatedEntity()
	{
		TestData testData = oriWrapperTestBed.getBuidUpdatedEntityTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeCreatedAndUpdatedEntities()
	{
		TestData testData = oriWrapperTestBed.getCreatedAndUpdatedEntitiesTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		if (!testData.xml.equals(xml))
		{
			// Compensate for loss of order in set in CUDResut
			testData = oriWrapperTestBed.getCreatedAndUpdatedEntitiesTestData2();
		}
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeCreatedAndExistingChildren()
	{
		TestData testData = oriWrapperTestBed.getCreatedAndExistingChildrenTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		if (!testData.xml.equals(xml))
		{
			// Compensate for loss of order in set in CUDResut
			testData = oriWrapperTestBed.getCreatedAndExistingChildrenTestData2();
		}
		Assert.assertEquals(testData.xml, xml);
	}
}
