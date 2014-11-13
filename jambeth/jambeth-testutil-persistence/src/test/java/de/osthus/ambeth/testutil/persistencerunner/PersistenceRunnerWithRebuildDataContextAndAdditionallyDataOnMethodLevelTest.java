package de.osthus.ambeth.testutil.persistencerunner;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLDataRebuild;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

/**
 * Test for the AmbethPersistenceRunner which has no {@link SQLStructure} (using an existing structure) but the {@link SQLDataRebuild} and the {@link SQLData}
 * annotations (on class and method level) set (or set to true) and has an annotation set (on class level) which would trigger a context rebuild like
 * {@link TestModule} or {@link TestProperties}.
 * <p>
 * Attention: the alternate user has to exist (use script testutil_alternate_test_structure.sql).
 */
@TestPropertiesList({ @TestProperties(type = AlternativeUserPropertiesProvider.class),//
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/testutil/persistencerunner/testutil_test_orm.xml") })
@SQLData("testutil_test_data_0.sql")
@SQLDataRebuild(value = true)
public class PersistenceRunnerWithRebuildDataContextAndAdditionallyDataOnMethodLevelTest extends AbstractPersistenceRunnerTest
{
	@Test
	@SQLData("testutil_test_data_1.sql")
	public void testDummy1()
	{
		assertDataIsExactly(Arrays.asList(11, 12, 13, 101));
	}

	@Test
	public void testDummy2()
	{
		assertDataIsExactly(Arrays.asList(11, 12, 13));
	}

	@Test
	@SQLData("testutil_test_data_2.sql")
	public void testDummy3()
	{
		assertDataIsExactly(Arrays.asList(11, 12, 13, 102));
	}

	@Test
	public void testDummy4()
	{
		assertDataIsExactly(Arrays.asList(11, 12, 13));
	}

	@Test
	@SQLData("testutil_test_data_5.sql")
	public void testDummy5()
	{
		assertDataIsExactly(Arrays.asList(11, 12, 13, 105));
	}

	@AfterClass
	public static void testAfterClass()
	{
		int numberOfTestMethods = currentRunner.getDescription().testCount();
		assertAllCountsExactly(1, 0, numberOfTestMethods, numberOfTestMethods);
	}

}
