package de.osthus.ambeth.testutil.persistencerunner;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Test;

import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLDataRebuild;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

/**
 * Test for the AmbethPersistenceRunner which has no {@link SQLStructure} (using an existing structure) and no {@link SQLDataRebuild} flag set but the
 * {@link SQLData} annotations (on class and method level)and has an annotation set (on class level) which would trigger a context rebuild like
 * {@link TestModule} or {@link TestProperties}.
 * <p>
 * Attention: this test class relies on the test method order (which is not guaranteed)
 * <p>
 * Attention: the alternate user has to exist (use script testutil_alternate_test_structure.sql).
 */
@TestPropertiesList({
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName, value = "jambeth_testutil_user"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseUser, value = "${" + PersistenceJdbcConfigurationConstants.DatabaseSchemaName
				+ "}"), @TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/testutil/persistencerunner/testutil_test_orm.xml") })
@SQLData("testutil_test_data_0.sql")
@SQLDataRebuild(value = false)
public class PersistenceRunnerWithContextAndAdditionallyDataOnMethodLevelTest extends AbstractPersistenceRunnerTest
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
		assertDataIsExactly(Arrays.asList(11, 12, 13, 101));
	}

	@Test
	@SQLData("testutil_test_data_2.sql")
	public void testDummy3()
	{
		assertDataIsExactly(Arrays.asList(11, 12, 13, 101, 102));
	}

	@Test
	public void testDummy4()
	{
		assertDataIsExactly(Arrays.asList(11, 12, 13, 101, 102));
	}

	@Test
	@SQLData("testutil_test_data_5.sql")
	public void testDummy5()
	{
		assertDataIsExactly(Arrays.asList(11, 12, 13, 101, 102, 105));
	}

	@AfterClass
	public static void testAfterClass()
	{
		assertAllCountsExactly(1, 0, 1, 1);
	}

}
