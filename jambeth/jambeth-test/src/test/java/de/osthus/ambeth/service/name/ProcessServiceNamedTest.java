package de.osthus.ambeth.service.name;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.BootstrapScannerModule;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.service.IProcessService;
import de.osthus.ambeth.service.SyncToAsyncUtil;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.transfer.ServiceDescription;
import de.osthus.ambeth.xml.ICyclicXMLHandler;
import de.osthus.ambeth.xml.IXmlTypeExtendable;

@SQLStructure("../ProcessServiceTest_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/service/orm.xml")
@TestModule({ BootstrapScannerModule.class, XmlModule.class, ProcessServiceNamedTestModule.class })
public class ProcessServiceNamedTest extends AbstractInformationBusWithPersistenceTest
{
	private static final Object[] NO_PARAMS = {};

	@Autowired
	private IProcessService fixture;

	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	private ICyclicXMLHandler cyclicXMLHandler;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IXmlTypeExtendable xmlTypeExtendable;

	@Test
	public void testCallToService() throws Exception
	{
		Class<?> serviceType = de.osthus.ambeth.transfer.ITestService.class;
		Method syncMethod = serviceType.getMethod("noParamNoReturn");
		ServiceDescription serviceDescription = SyncToAsyncUtil.createServiceDescription("TestService", syncMethod, NO_PARAMS);

		String xml = cyclicXMLHandler.write(serviceDescription);
		ServiceDescription actual = (ServiceDescription) cyclicXMLHandler.read(xml);

		assertEquals(serviceDescription.getMethod(serviceType, objectCollector), actual.getMethod(serviceType, objectCollector));

		fixture.invokeService(actual);
	}

	@Test
	public void testCallToService2() throws Exception
	{
		Class<?> serviceType = de.osthus.ambeth.service.name.ITestService.class;
		Method syncMethod = serviceType.getMethod("testCallOnNamedService");
		ServiceDescription serviceDescription = SyncToAsyncUtil.createServiceDescription("TestService 2", syncMethod, NO_PARAMS);

		String xml = cyclicXMLHandler.write(serviceDescription);
		ServiceDescription actual = (ServiceDescription) cyclicXMLHandler.read(xml);

		assertEquals(serviceDescription.getMethod(serviceType, objectCollector), actual.getMethod(serviceType, objectCollector));

		fixture.invokeService(actual);
	}

	@Test
	public void testRegisterExtendingInterface() throws Exception
	{
		Class<?> rootElementClass = de.osthus.ambeth.service.name.other2.ITestService.class;
		xmlTypeExtendable.registerXmlType(rootElementClass, rootElementClass.getSimpleName(), null);
	}

	@Test(expected = IllegalStateException.class)
	public void testRegisterMismatchingInterface() throws Exception
	{
		Class<?> rootElementClass = de.osthus.ambeth.service.name.other.ITestService.class;
		xmlTypeExtendable.registerXmlType(rootElementClass, rootElementClass.getSimpleName(), null);
	}
}
