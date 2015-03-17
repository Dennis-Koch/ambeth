package de.osthus.ambeth.transfer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.BootstrapScannerModule;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.model.MaterialGroup;
import de.osthus.ambeth.service.ProcessServiceTestModule;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.xml.ICyclicXMLHandler;

@SQLStructure("../service/ProcessServiceTest_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/service/orm.xml")
@TestModule({ BootstrapScannerModule.class, XmlModule.class, ProcessServiceTestModule.class })
public class ServiceResultDeSerializationTest extends AbstractInformationBusWithPersistenceTest
{
	@Autowired
	protected ICache cache;

	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXMLHandler;

	@Test
	public void testNoReturn() throws Exception
	{
		runTest(null);
	}

	@Test
	public void testPrimitiveReturn() throws Exception
	{
		runTest(2);
	}

	@Test
	public void testPrimitiveArrayReturn() throws Exception
	{
		int[] returnValue = new int[] { 1, 23, 4 };
		int[] actual = (int[]) runTest(returnValue);
		assertArrayEquals(returnValue, actual);
	}

	@Test
	public void testPrimitiveListReturn() throws Exception
	{
		Object returnValue = Arrays.asList(1, 2, 3, 4);
		runTest(returnValue);
	}

	@Test
	public void testDateReturn() throws Exception
	{
		Object returnValue = new Date();
		runTest(returnValue);
	}

	@Test
	@SQLData("../service/ProcessServiceTest_data.sql")
	public void testEntityReturn() throws Exception
	{
		MaterialGroup returnValue = cache.getObject(MaterialGroup.class, "1");
		runTest(returnValue);
	}

	@Test
	public void testEntityRelationReturn() throws Exception
	{
		Material returnValue = cache.getObject(Material.class, 1);
		runTest(returnValue);
	}

	private Object runTest(Object returnValue) throws NoSuchMethodException
	{
		String xml = cyclicXMLHandler.write(returnValue);
		Object actual = cyclicXMLHandler.read(xml);
		if (actual == null || !actual.getClass().isArray())
		{
			assertEquals(returnValue, actual);
		}
		return actual;
	}
}
