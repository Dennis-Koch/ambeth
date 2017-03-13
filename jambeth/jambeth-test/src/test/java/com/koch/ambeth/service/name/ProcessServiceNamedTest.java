package com.koch.ambeth.service.name;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.service.IProcessService;
import com.koch.ambeth.service.SyncToAsyncUtil;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.transfer.ServiceDescription;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.xml.ICyclicXMLHandler;
import com.koch.ambeth.xml.IXmlTypeExtendable;
import com.koch.ambeth.xml.ioc.BootstrapScannerModule;
import com.koch.ambeth.xml.ioc.XmlModule;

@SQLStructure("../ProcessServiceTest_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/service/orm.xml")
@TestModule({BootstrapScannerModule.class, XmlModule.class, ProcessServiceNamedTestModule.class})
public class ProcessServiceNamedTest extends AbstractInformationBusWithPersistenceTest {
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
	public void testCallToService() throws Exception {
		Class<?> serviceType = com.koch.ambeth.transfer.ITestService.class;
		Method syncMethod = serviceType.getMethod("noParamNoReturn");
		ServiceDescription serviceDescription =
				SyncToAsyncUtil.createServiceDescription("TestService", syncMethod, NO_PARAMS);

		String xml = cyclicXMLHandler.write(serviceDescription);
		ServiceDescription actual = (ServiceDescription) cyclicXMLHandler.read(xml);

		assertEquals(serviceDescription.getMethod(serviceType, objectCollector),
				actual.getMethod(serviceType, objectCollector));

		fixture.invokeService(actual);
	}

	@Test
	public void testCallToService2() throws Exception {
		Class<?> serviceType = com.koch.ambeth.service.name.ITestService.class;
		Method syncMethod = serviceType.getMethod("testCallOnNamedService");
		ServiceDescription serviceDescription =
				SyncToAsyncUtil.createServiceDescription("TestService 2", syncMethod, NO_PARAMS);

		String xml = cyclicXMLHandler.write(serviceDescription);
		ServiceDescription actual = (ServiceDescription) cyclicXMLHandler.read(xml);

		assertEquals(serviceDescription.getMethod(serviceType, objectCollector),
				actual.getMethod(serviceType, objectCollector));

		fixture.invokeService(actual);
	}

	@Test
	public void testRegisterExtendingInterface() throws Exception {
		Class<?> rootElementClass = com.koch.ambeth.service.name.other2.ITestService.class;
		xmlTypeExtendable.registerXmlType(rootElementClass, rootElementClass.getSimpleName(), null);
	}

	@Test(expected = IllegalStateException.class)
	public void testRegisterMismatchingInterface() throws Exception {
		Class<?> rootElementClass = com.koch.ambeth.service.name.other.ITestService.class;
		xmlTypeExtendable.registerXmlType(rootElementClass, rootElementClass.getSimpleName(), null);
	}
}
