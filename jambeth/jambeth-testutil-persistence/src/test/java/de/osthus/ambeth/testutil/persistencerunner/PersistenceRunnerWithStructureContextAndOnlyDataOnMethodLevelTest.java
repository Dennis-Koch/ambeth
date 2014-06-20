package de.osthus.ambeth.testutil.persistencerunner;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLDataList;
import de.osthus.ambeth.testutil.SQLDataRebuild;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

/**
 * Test for the AmbethPersistenceRunner which has the {@link SQLStructure} and the {@link SQLData} annotations (only on method level) set but not the
 * {@link SQLDataRebuild} flag and has an annotation set (on class level) which would trigger a context rebuild like {@link TestModule} or
 * {@link TestProperties}.
 * <p>
 * Attention: this test class relies on the test method order (which is not guaranteed)
 */
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/testutil/persistencerunner/testutil_test_orm.xml") })
@SQLStructure("testutil_test_structure.sql")
@SQLDataRebuild(value = false)
public class PersistenceRunnerWithStructureContextAndOnlyDataOnMethodLevelTest extends AbstractPersistenceRunnerTest
{

	@Test
	@SQLData("testutil_test_data_1.sql")
	public void testDummy1()
	{
		assertDataIsExactly(Arrays.asList(101));
	}

	@Test
	public void testDummy2()
	{
		assertDataIsExactly(Arrays.asList(101));
	}

	@Test
	@SQLDataList({ @SQLData("testutil_test_data_2.sql"), @SQLData("testutil_test_data_3.sql") })
	public void testDummy3()
	{
		assertDataIsExactly(Arrays.asList(101, 102, 103));
	}

	@Test
	public void testDummy4()
	{
		assertDataIsExactly(Arrays.asList(101, 102, 103));
	}

	@Test
	@SQLData("testutil_test_data_5.sql")
	public void testDummy5()
	{
		assertDataIsExactly(Arrays.asList(101, 102, 103, 105));
	}

	@AfterClass
	public static void testAfterClass()
	{
		assertAllCountsExactly(1, 1, 0, 0);
	}

}
