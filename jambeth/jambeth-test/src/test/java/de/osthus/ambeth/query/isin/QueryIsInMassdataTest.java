package de.osthus.ambeth.query.isin;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLDataRebuild;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.testutil.category.PerformanceTests;

@Category(PerformanceTests.class)
@TestModule(QueryIsInMassdataTestModule.class)
@SQLDataRebuild(false)
@SQLData("QueryIsInMassdata_data.sql")
@SQLStructure("QueryIsInMassdata_structure.sql")
@TestPropertiesList({
		@TestProperties(name = PersistenceConfigurationConstants.AutoIndexForeignKeys, value = "false"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabasePoolMaxUsed, value = "20"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/query/isin/QueryIsInMassdata_orm.xml"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.cache.DefaultPersistenceCacheRetriever", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.filter.PagingQuery", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.orm.XmlDatabaseMapper", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.persistence.EntityLoader", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.persistence.jdbc.JdbcTable", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.persistence.jdbc.JDBCDatabaseWrapper", value = "INFO"),
		// @TestProperties(name = "ambeth.log.level.de.osthus.ambeth.persistence.jdbc.connection.LogPreparedStatementInterceptor", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.persistence.jdbc.connection.LogStatementInterceptor", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.persistence.jdbc.database.JdbcTransaction", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.proxy.AbstractCascadePostProcessor", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.cache.CacheLocalDataChangeListener", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.cache.FirstLevelCacheManager", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.service.MergeService", value = "INFO") })
public class QueryIsInMassdataTest extends AbstractPersistenceTest
{
	protected static long timeForEquals = 0;

	protected static long timeForIsIn = 0;

	@Autowired
	protected IChildService childService;

	@Test
	public void testTimeForEquals() throws Exception
	{
		long start = System.currentTimeMillis();
		childService.searchForParentWithEquals(10001);
		timeForEquals = System.currentTimeMillis() - start;
		checkTimes();
	}

	@Test
	public void testTimeForIsIn() throws Exception
	{
		long start = System.currentTimeMillis();
		childService.getForParentWithIsIn(10002);
		timeForIsIn = System.currentTimeMillis() - start;
		checkTimes();
	}

	@Test
	public void testTimeForIsInMoreThan4000() throws Exception
	{
		int[] parentIds = new int[4005]; // Force UNION > 4000
		for (int a = parentIds.length; a-- > 0;)
		{
			parentIds[a] = 10000 + a;
		}
		long start = System.currentTimeMillis();
		childService.getForParentWithIsIn(parentIds);
		timeForIsIn = System.currentTimeMillis() - start;
		checkTimes();
	}

	private void checkTimes()
	{
		if (timeForEquals > 0 && timeForIsIn > 0)
		{
			if (timeForEquals < 50)
			{
				Assert.fail("Difference not significant. Use a larger number of entities.");
			}
			if (timeForIsIn > timeForEquals * 1.5)
			{
				Assert.fail("IsIn is to slow: timeForEquals = " + timeForEquals + ", timeForIsIn = " + timeForIsIn);
			}
		}
	}
}
