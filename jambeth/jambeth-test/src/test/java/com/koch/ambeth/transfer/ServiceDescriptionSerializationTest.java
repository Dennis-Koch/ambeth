package com.koch.ambeth.transfer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.model.MaterialGroup;
import com.koch.ambeth.service.ProcessServiceTestModule;
import com.koch.ambeth.service.SyncToAsyncUtil;
import com.koch.ambeth.service.TestService;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.transfer.ServiceDescription;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.objectcollector.IObjectCollector;
import com.koch.ambeth.xml.ICyclicXMLHandler;
import com.koch.ambeth.xml.ioc.BootstrapScannerModule;
import com.koch.ambeth.xml.ioc.XmlModule;

@SQLStructure("../service/ProcessServiceTest_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/service/orm.xml")
@TestModule({BootstrapScannerModule.class, XmlModule.class, ProcessServiceTestModule.class})
public class ServiceDescriptionSerializationTest extends AbstractInformationBusWithPersistenceTest {
	@Autowired
	protected ICache cache;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXMLHandler;

	@Autowired
	protected IObjectCollector objectCollector;

	@Test
	public void testNoParamNoReturn() throws Exception {
		String methodName = "noParamNoReturn";
		Class<?>[] paramTypes = {};
		Object[] params = {};
		runTest(methodName, paramTypes, params);
	}

	@Test
	public void testPrimitiveParamNoReturn() throws Exception {
		String methodName = "primitiveParamNoReturn";
		Class<?>[] paramTypes = {int.class};
		Object[] params = {5};
		runTest(methodName, paramTypes, params);
	}

	@Test
	public void testDateParamNoReturn() throws Exception {
		String methodName = "dateParamNoReturn";
		Class<?>[] paramTypes = {Date.class};
		Object[] params = {new Date()};
		runTest(methodName, paramTypes, params);
	}

	@Test
	public void testPrimitiveArrayParamNoReturn() throws Exception {
		String methodName = "primitiveArrayParamNoReturn";
		Class<?>[] paramTypes = {int[].class};
		Object[] params = {new int[] {1, 2, 3, 4}};
		runTest(methodName, paramTypes, params);
	}

	@Test
	public void testPrimitiveListParamNoReturn() throws Exception {
		String methodName = "primitiveListParamNoReturn";
		Class<?>[] paramTypes = {List.class};
		Object[] params = {Arrays.asList(1, 2, 3, 4)};
		runTest(methodName, paramTypes, params);
	}

	@Test
	@SQLData("../service/ProcessServiceTest_data.sql")
	public void testEntityParamNoReturn() throws Exception {
		String methodName = "entityParamNoReturn";
		Class<?>[] paramTypes = {MaterialGroup.class};
		MaterialGroup materialGroup = cache.getObject(MaterialGroup.class, "1");
		Object[] params = {materialGroup};
		runTest(methodName, paramTypes, params);
	}

	@Test
	@SQLData("../service/ProcessServiceTest_data.sql")
	public void testEntityWithRelationParamNoReturn() throws Exception {
		String methodName = "entityWithRelationParamNoReturn";
		Class<?>[] paramTypes = {Material.class};
		Material material = cache.getObject(Material.class, 1);
		Object[] params = {material};
		runTest(methodName, paramTypes, params);
	}

	// TODO Serialize changed entities as UpdateContainer
	@Test
	@SQLData("../service/ProcessServiceTest_data.sql")
	public void testChangedEntityParamNoReturn() throws Exception {
		ICache cache = cacheFactory.create(CacheFactoryDirective.NoDCE, "test");

		String methodName = "entityParamNoReturn";
		Class<?>[] paramTypes = {MaterialGroup.class};
		MaterialGroup materialGroup = cache.getObject(MaterialGroup.class, "1");
		materialGroup.setName(materialGroup.getName() + " new");
		Object[] params = {materialGroup};
		Object[] actualParams = runTest(methodName, paramTypes, params);
		Assert.assertEquals(((MaterialGroup) params[0]).getName(),
				((MaterialGroup) actualParams[0]).getName());
	}

	private Object[] runTest(String methodName, Class<?>[] paramTypes, Object[] params)
			throws NoSuchMethodException {
		Method serviceMethod = TestService.class.getMethod(methodName, paramTypes);

		ServiceDescription expectedServiceDescription =
				SyncToAsyncUtil.createServiceDescription("TestService", serviceMethod, params);
		validateServiceDescription(expectedServiceDescription, TestService.class, "TestService",
				serviceMethod, params);

		String xml = cyclicXMLHandler.write(expectedServiceDescription);

		ServiceDescription actualServiceDescription = (ServiceDescription) cyclicXMLHandler.read(xml);
		validateServiceDescription(actualServiceDescription, TestService.class, "TestService",
				serviceMethod, params);
		com.koch.ambeth.transfer.Assert.assertEquals(expectedServiceDescription,
				actualServiceDescription);

		return actualServiceDescription.getArguments();
	}

	private void validateServiceDescription(ServiceDescription serviceDescription,
			Class<?> serviceType, String string, Method serviceMethod, Object[] arguments) {
		Assert.assertNotNull(serviceDescription);
		Assert.assertEquals("TestService", serviceDescription.getServiceName());
		Assert.assertEquals(serviceMethod, serviceDescription.getMethod(serviceType, objectCollector));
		Assert.assertArrayEquals(arguments, serviceDescription.getArguments());
	}
}
