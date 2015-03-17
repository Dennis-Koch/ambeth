package de.osthus.esmeralda.handler;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class ASTHelperTest
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	private ASTHelper astHelper;

	@Before
	public void setUp() throws Exception
	{
		astHelper = new ASTHelper();
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testExtractNonGenericType()
	{
		String nonGenericType;
		nonGenericType = astHelper.extractNonGenericType("java.lang.String");
		Assert.assertNotNull(nonGenericType);
		Assert.assertEquals("java.lang.String", nonGenericType);

		nonGenericType = astHelper.extractNonGenericType("java.util.List<Class<?>>");
		Assert.assertNotNull(nonGenericType);
		Assert.assertEquals("java.util.List", nonGenericType);
	}

	@Test
	public void testParseGenericType()
	{
		String[] nonGenericType;

		nonGenericType = astHelper.parseGenericType("java.lang.String");
		Assert.assertNotNull(nonGenericType);
		Assert.assertEquals(1, nonGenericType.length);
		Assert.assertEquals("java.lang.String", nonGenericType[0]);

		nonGenericType = astHelper.parseGenericType("java.util.List<Class<?>>");
		Assert.assertNotNull(nonGenericType);
		Assert.assertEquals(2, nonGenericType.length);
		Assert.assertEquals("java.util.List", nonGenericType[0]);
		Assert.assertEquals("Class<?>", nonGenericType[1]);

		nonGenericType = astHelper.parseGenericType("java.util.List<A><B>");
		Assert.assertNotNull(nonGenericType);
		Assert.assertEquals(2, nonGenericType.length);
		Assert.assertEquals("java.util.List", nonGenericType[0]);
		Assert.assertEquals("B", nonGenericType[1]);

		String typeName = "de.osthus.ambeth.collections.HashMap<A,B><java.lang.reflect.AnnotatedElement<Class<?>>,de.osthus.ambeth.annotation.AnnotationEntry<T>>";
		nonGenericType = astHelper.parseGenericType(typeName);
		Assert.assertNotNull(nonGenericType);
		Assert.assertEquals(2, nonGenericType.length);
		Assert.assertEquals("de.osthus.ambeth.collections.HashMap", nonGenericType[0]);
		Assert.assertEquals("java.lang.reflect.AnnotatedElement<Class<?>>,de.osthus.ambeth.annotation.AnnotationEntry<T>", nonGenericType[1]);
	}
}
