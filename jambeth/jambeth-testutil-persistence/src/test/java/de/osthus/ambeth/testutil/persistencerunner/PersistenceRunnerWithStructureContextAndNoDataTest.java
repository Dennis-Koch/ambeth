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
 * Test for the AmbethPersistenceRunner which has the {@link SQLStructure} but no {@link SQLData} annotation and the {@link SQLDataRebuild} flag set and has an
 * annotation set (on class level) which would trigger a context rebuild like {@link TestModule} or {@link TestProperties}.
 * <p>
 * Attention: this test class relies on the test method order (which is not guaranteed)
 */
@TestPropertiesList({ @TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/testutil/persistencerunner/testutil_test_orm.xml") })
@SQLStructure("testutil_test_structure.sql")
@SQLDataRebuild(value = false)
public class PersistenceRunnerWithStructureContextAndNoDataTest extends AbstractPersistenceRunnerWithServiceTest
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
		assertDataIsExactly(Arrays.asList(1002));
	}

	@Test
	public void testDummy4()
	{
		insertData(1004);
		assertDataIsExactly(Arrays.asList(1002, 1004));
	}

	@Test
	public void testDummy5()
	{
		assertDataIsExactly(Arrays.asList(1002, 1004));
	}

	@AfterClass
	public static void testAfterClass()
	{
		assertAllCountsExactly(1, 1, 0, 0);
	}

}