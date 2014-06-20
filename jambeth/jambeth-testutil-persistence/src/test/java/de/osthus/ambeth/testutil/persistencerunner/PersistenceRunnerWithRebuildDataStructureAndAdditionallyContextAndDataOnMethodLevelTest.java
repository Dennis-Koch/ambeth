package de.osthus.ambeth.testutil.persistencerunner;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLDataRebuild;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

/**
 * Test for the AmbethPersistenceRunner which has the {@link SQLStructure}, the {@link SQLDataRebuild} and the {@link SQLData} annotations (on class and method
 * level) set (or set to true) and has annotations set on class and method level which would trigger a context rebuild like {@link TestModule} or
 * {@link TestProperties}.
 */
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/testutil/persistencerunner/testutil_test_orm.xml") })
@SQLStructure("testutil_test_structure.sql")
@SQLData("testutil_test_data_0.sql")
@SQLDataRebuild(value = true)
public class PersistenceRunnerWithRebuildDataStructureAndAdditionallyContextAndDataOnMethodLevelTest extends AbstractPersistenceRunnerTest
{

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "10")
	@SQLData("testutil_test_data_1.sql")
	public void testDummy1()
	{
		assertDataIsExactly(Arrays.asList(11, 12, 13, 101));
	}

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "20")
	@SQLData("testutil_test_data_2.sql")
	public void testDummy2()
	{
		assertDataIsExactly(Arrays.asList(11, 12, 13, 102));
	}

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "30")
	@SQLData("testutil_test_data_3.sql")
	public void testDummy3()
	{
		assertDataIsExactly(Arrays.asList(11, 12, 13, 103));
	}

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "40")
	@SQLData("testutil_test_data_4.sql")
	public void testDummy4()
	{
		assertDataIsExactly(Arrays.asList(11, 12, 13, 104));
	}

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "50")
	@SQLData("testutil_test_data_5.sql")
	public void testDummy5()
	{
		assertDataIsExactly(Arrays.asList(11, 12, 13, 105));
	}

	@AfterClass
	public static void testAfterClass()
	{
		int numberOfTestMethods = currentRunner.getDescription().testCount();
		assertAllCountsExactly(numberOfTestMethods, 1, numberOfTestMethods, numberOfTestMethods);
	}

}
