package de.osthus.ambeth.testutil.persistencerunner;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import de.osthus.ambeth.IDatabasePool;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLDataRebuild;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.ParamChecker;

/**
 * Test for the AmbethPersistenceRunner which has neither the {@link SQLStructure}, the {@link SQLDataRebuild} or the {@link SQLData} annotations set (or set to
 * true) and has none of the annotations which would trigger a context rebuild like {@link TestModule} or {@link TestProperties}.
 */
@SQLDataRebuild(false)
public class PersistenceRunnerWithoutAnythingTest extends AbstractPersistenceRunnerTest
{

	private IDatabasePool dbPool;

	private static List<String> methodCalls = new ArrayList<String>();

	public void setDbPool(IDatabasePool dbPool)
	{
		this.dbPool = dbPool;
		methodCalls.add("Successful injection");
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
		ParamChecker.assertNotNull(this.dbPool, "dbPool");
	}

	@Before
	public void beforeTestMethod()
	{
		methodCalls.add("Before test method");
	}

	@Test
	public void testDummy1()
	{
	}

	@Test
	public void testDummy2()
	{
	}

	@Test
	public void testDummy3()
	{
	}

	@Test
	public void testDummy4()
	{
	}

	@Test
	public void testDummy5()
	{
	}

	@AfterClass
	public static void testAfterClass()
	{
		assertAllCountsExactly(1, 0, 0, 0);
		Assert.assertEquals(currentRunner.testCount() * 2, methodCalls.size());
		for (int i = 0; i < methodCalls.size(); i += 2)
		{
			Assert.assertEquals("First method call was not the expected one!", "Successful injection", methodCalls.get(i));
			Assert.assertEquals("Second method call was not the expected one!", "Before test method", methodCalls.get(i + 1));
		}
	}

}
