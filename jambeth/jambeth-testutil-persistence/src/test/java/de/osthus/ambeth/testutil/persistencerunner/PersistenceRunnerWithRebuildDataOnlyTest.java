package de.osthus.ambeth.testutil.persistencerunner;

import org.junit.AfterClass;
import org.junit.Test;

import de.osthus.ambeth.testutil.SQLDataRebuild;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;

/**
 * Test for the AmbethPersistenceRunner which only the {@link SQLDataRebuild} annotation set (to true) and has none of the annotations which would trigger a
 * context rebuild like {@link TestModule} or {@link TestProperties}.
 */
@SQLDataRebuild(true)
public class PersistenceRunnerWithRebuildDataOnlyTest extends AbstractPersistenceRunnerTest
{

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
		int numberOfTestMethods = currentRunner.getDescription().testCount();
		assertAllCountsExactly(1, 0, numberOfTestMethods, numberOfTestMethods);
	}

}
