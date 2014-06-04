package de.osthus.ambeth.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.model.MaterialGroup;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.transfer.ITestService;
import de.osthus.ambeth.transfer.ServiceDescription;
import de.osthus.ambeth.util.ParamChecker;

@SQLStructure("ProcessServiceTest_structure.sql")
@TestModule(ProcessServiceTestModule.class)
@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/service/orm.xml")
public class ProcessServiceTest extends AbstractPersistenceTest
{
	private static final Object[] NO_PARAMS = {};

	private IProcessService fixture;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(fixture, "fixture");
	}

	public void setProcessService(IProcessService processService)
	{
		fixture = processService;
	}

	@Test
	public void testInvokeService_primitiveReturn() throws Exception
	{
		Object actual = invokeServiceMethod("noParamPrimitiveReturn");
		assertNotNull(actual);
		assertEquals(1, actual);
	}

	@Test
	public void testInvokeService_primitiveArrayReturn() throws Exception
	{
		Object actual = invokeServiceMethod("noParamPrimitiveArrayReturn");
		assertNotNull(actual);
		int[] expected = { 1, 2, 34 };
		assertArrayEquals(expected, (int[]) actual);
	}

	@Test
	public void testInvokeService_primitiveListReturn() throws Exception
	{
		Object actual = invokeServiceMethod("noParamPrimitiveListReturn");
		assertNotNull(actual);
		@SuppressWarnings("unchecked")
		List<Integer> actualList = (List<Integer>) actual;
		Integer[] actualArray = actualList.toArray(new Integer[actualList.size()]);
		Integer[] expected = { 12, 3, 4 };
		assertArrayEquals(expected, actualArray);
	}

	@Test
	public void testInvokeService_dateReturn() throws Exception
	{
		Object actual = invokeServiceMethod("noParamDateReturn");
		assertNotNull(actual);
		long actualTime = ((Date) actual).getTime();
		long difference = System.currentTimeMillis() - actualTime;
		assertTrue(difference < 100);
	}

	@Test
	@SQLData("ProcessServiceTest_data.sql")
	public void testInvokeService_entityReturn() throws Exception
	{
		Object actual = invokeServiceMethod("noParamEntityReturn");
		assertNotNull(actual);
		assertTrue(actual instanceof MaterialGroup);
	}

	@Test
	@SQLData("ProcessServiceTest_data.sql")
	public void testInvokeService_entityWithRelationReturn() throws Exception
	{
		Object actual = invokeServiceMethod("noParamEntityWithRelationReturn");
		assertNotNull(actual);
		assertTrue(actual instanceof Material);
		Material material = (Material) actual;
		assertNotNull(material.getMaterialGroup());
	}

	private Object invokeServiceMethod(String methodName) throws NoSuchMethodException
	{
		Method serviceMethod = ITestService.class.getMethod(methodName);
		ServiceDescription serviceDescription = SyncToAsyncUtil.createServiceDescription(TestService.class.getSimpleName(), serviceMethod, NO_PARAMS);
		Object actual = fixture.invokeService(serviceDescription);
		return actual;
	}
}