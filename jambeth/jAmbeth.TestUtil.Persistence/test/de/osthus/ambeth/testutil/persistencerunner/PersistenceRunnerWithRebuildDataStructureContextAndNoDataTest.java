package de.osthus.ambeth.testutil.persistencerunner;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Test;

import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLDataRebuild;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

/**
 * Test for the AmbethPersistenceRunner which has the {@link SQLStructure} and the {@link SQLDataRebuild} but no {@link SQLData} annotations set (or set to
 * true) and has an annotation set (on class level) which would trigger a context rebuild like {@link TestModule} or {@link TestProperties}.
 */
@TestPropertiesList({ @TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/testutil/persistencerunner/testutil_test_orm.xml") })
@SQLStructure("testutil_test_structure.sql")
@SQLDataRebuild(value = true)
public class PersistenceRunnerWithRebuildDataStructureContextAndNoDataTest extends AbstractPersistenceRunnerWithServiceTest
{

	@Test
	public void testDummy1()
	{
		assertDataIsExactly(null);
	}

	@Test
	public void testDummy2()
	{
		assertDataIsExactly(null);
		insertData(1002);
		assertDataIsExactly(Arrays.asList(1002));
	}

	@Test
	public void testDummy3()
	{
		assertDataIsExactly(null);
	}

	@Test
	public void testDummy4()
	{
		assertDataIsExactly(null);
		insertData(1004);
		assertDataIsExactly(Arrays.asList(1004));
	}

	@Test
	public void testDummy5()
	{
		assertDataIsExactly(null);
	}

	@AfterClass
	public static void testAfterClass()
	{
		int numberOfTestMethods = currentRunner.getDescription().testCount();
		assertAllCountsExactly(1, 1, numberOfTestMethods, numberOfTestMethods);
	}

}
