package de.osthus.ambeth.transfer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.BootstrapScannerModule;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.model.MaterialGroup;
import de.osthus.ambeth.objectcollector.IObjectCollector;
import de.osthus.ambeth.service.ProcessServiceTestModule;
import de.osthus.ambeth.service.SyncToAsyncUtil;
import de.osthus.ambeth.service.TestService;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.xml.ICyclicXMLHandler;

@SQLStructure("../service/ProcessServiceTest_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/service/orm.xml")
@TestModule({ BootstrapScannerModule.class, XmlModule.class, ProcessServiceTestModule.class })
public class ServiceDescriptionSerializationTest extends AbstractInformationBusWithPersistenceTest
{
	@Autowired
	protected ICache cache;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXMLHandler;

	@Autowired
	protected IObjectCollector objectCollector;

	@Test
	public void testNoParamNoReturn() throws Exception
	{
		String methodName = "noParamNoReturn";
		Class<?>[] paramTypes = {};
		Object[] params = {};
		runTest(methodName, paramTypes, params);
	}

	@Test
	public void testPrimitiveParamNoReturn() throws Exception
	{
		String methodName = "primitiveParamNoReturn";
		Class<?>[] paramTypes = { int.class };
		Object[] params = { 5 };
		runTest(methodName, paramTypes, params);
	}

	@Test
	public void testDateParamNoReturn() throws Exception
	{
		String methodName = "dateParamNoReturn";
		Class<?>[] paramTypes = { Date.class };
		Object[] params = { new Date() };
		runTest(methodName, paramTypes, params);
	}

	@Test
	public void testPrimitiveArrayParamNoReturn() throws Exception
	{
		String methodName = "primitiveArrayParamNoReturn";
		Class<?>[] paramTypes = { int[].class };
		Object[] params = { new int[] { 1, 2, 3, 4 } };
		runTest(methodName, paramTypes, params);
	}

	@Test
	public void testPrimitiveListParamNoReturn() throws Exception
	{
		String methodName = "primitiveListParamNoReturn";
		Class<?>[] paramTypes = { List.class };
		Object[] params = { Arrays.asList(1, 2, 3, 4) };
		runTest(methodName, paramTypes, params);
	}

	@Test
	@SQLData("../service/ProcessServiceTest_data.sql")
	public void testEntityParamNoReturn() throws Exception
	{
		String methodName = "entityParamNoReturn";
		Class<?>[] paramTypes = { MaterialGroup.class };
		MaterialGroup materialGroup = cache.getObject(MaterialGroup.class, "1");
		Object[] params = { materialGroup };
		runTest(methodName, paramTypes, params);
	}

	@Test
	@SQLData("../service/ProcessServiceTest_data.sql")
	public void testEntityWithRelationParamNoReturn() throws Exception
	{
		String methodName = "entityWithRelationParamNoReturn";
		Class<?>[] paramTypes = { Material.class };
		Material material = cache.getObject(Material.class, 1);
		Object[] params = { material };
		runTest(methodName, paramTypes, params);
	}

	// TODO Serialize changed entities as UpdateContainer
	@Test
	@SQLData("../service/ProcessServiceTest_data.sql")
	public void testChangedEntityParamNoReturn() throws Exception
	{
		ICache cache = cacheFactory.create(CacheFactoryDirective.NoDCE, "test");

		String methodName = "entityParamNoReturn";
		Class<?>[] paramTypes = { MaterialGroup.class };
		MaterialGroup materialGroup = cache.getObject(MaterialGroup.class, "1");
		materialGroup.setName(materialGroup.getName() + " new");
		Object[] params = { materialGroup };
		Object[] actualParams = runTest(methodName, paramTypes, params);
		Assert.assertEquals(((MaterialGroup) params[0]).getName(), ((MaterialGroup) actualParams[0]).getName());
	}

	private Object[] runTest(String methodName, Class<?>[] paramTypes, Object[] params) throws NoSuchMethodException
	{
		Method serviceMethod = TestService.class.getMethod(methodName, paramTypes);

		ServiceDescription expectedServiceDescription = SyncToAsyncUtil.createServiceDescription("TestService", serviceMethod, params);
		validateServiceDescription(expectedServiceDescription, TestService.class, "TestService", serviceMethod, params);

		String xml = cyclicXMLHandler.write(expectedServiceDescription);

		ServiceDescription actualServiceDescription = (ServiceDescription) cyclicXMLHandler.read(xml);
		validateServiceDescription(actualServiceDescription, TestService.class, "TestService", serviceMethod, params);
		de.osthus.ambeth.transfer.Assert.assertEquals(expectedServiceDescription, actualServiceDescription);

		return actualServiceDescription.getArguments();
	}

	private void validateServiceDescription(ServiceDescription serviceDescription, Class<?> serviceType, String string, Method serviceMethod, Object[] arguments)
	{
		Assert.assertNotNull(serviceDescription);
		Assert.assertEquals("TestService", serviceDescription.getServiceName());
		Assert.assertEquals(serviceMethod, serviceDescription.getMethod(serviceType, objectCollector));
		Assert.assertArrayEquals(arguments, serviceDescription.getArguments());
	}
}
