package com.koch.ambeth.query.isin;

/*-
 * #%L
 * jambeth-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */
// package com.koch.ambeth.query.isin;
//
// import org.junit.Assert;
// import org.junit.Test;
// import org.junit.experimental.categories.Category;
//
// import com.koch.ambeth.config.ServiceConfigurationConstants;
// import com.koch.ambeth.ioc.annotation.Autowired;
// import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
// import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
// import com.koch.ambeth.testutil.SQLData;
// import com.koch.ambeth.testutil.SQLDataRebuild;
// import com.koch.ambeth.testutil.SQLStructure;
// import com.koch.ambeth.testutil.TestModule;
// import com.koch.ambeth.testutil.TestProperties;
// import com.koch.ambeth.testutil.TestPropertiesList;
// import com.koch.ambeth.testutil.category.PerformanceTests;
//
// @Category(PerformanceTests.class)
// @TestModule(QueryIsInMassdataTestModule.class)
// @SQLDataRebuild(false)
// @SQLData("QueryIsInMassdata_data.sql")
// @SQLStructure("QueryIsInMassdata_structure.sql")
// @TestPropertiesList({
// @TestProperties(name = PersistenceConfigurationConstants.AutoIndexForeignKeys, value = "false"),
// @TestProperties(name = PersistenceConfigurationConstants.DatabasePoolMaxUsed, value = "20"),
// @TestProperties(name = ServiceConfigurationConstants.mappingFile, value =
// "com/koch/ambeth/query/isin/QueryIsInMassdata_orm.xml"),
// @TestProperties(name = "ambeth.log.level.com.koch.ambeth.cache.DefaultPersistenceCacheRetriever",
// value = "INFO"),
// @TestProperties(name = "ambeth.log.level.com.koch.ambeth.filter.PagingQuery", value = "INFO"),
// @TestProperties(name = "ambeth.log.level.com.koch.ambeth.orm.XmlDatabaseMapper", value = "INFO"),
// @TestProperties(name = "ambeth.log.level.com.koch.ambeth.persistence.EntityLoader", value =
// "INFO"),
// @TestProperties(name = "ambeth.log.level.com.koch.ambeth.persistence.jdbc.JdbcTable", value =
// "INFO"),
// @TestProperties(name = "ambeth.log.level.com.koch.ambeth.persistence.jdbc.JDBCDatabaseWrapper",
// value = "INFO"),
// // @TestProperties(name =
// "ambeth.log.level.com.koch.ambeth.persistence.jdbc.connection.LogPreparedStatementInterceptor",
// value = "INFO"),
// @TestProperties(name =
// "ambeth.log.level.com.koch.ambeth.persistence.jdbc.connection.LogStatementInterceptor", value =
// "INFO"),
// @TestProperties(name =
// "ambeth.log.level.com.koch.ambeth.persistence.jdbc.database.JdbcTransaction", value = "INFO"),
// @TestProperties(name = "ambeth.log.level.com.koch.ambeth.proxy.AbstractCascadePostProcessor",
// value = "INFO"),
// @TestProperties(name = "ambeth.log.level.com.koch.ambeth.cache.CacheLocalDataChangeListener",
// value = "INFO"),
// @TestProperties(name = "ambeth.log.level.com.koch.ambeth.cache.FirstLevelCacheManager", value =
// "INFO"),
// @TestProperties(name = "ambeth.log.level.com.koch.ambeth.service.MergeService", value = "INFO")
// })
// public class QueryIsInMassdataTest extends AbstractInformationBusWithPersistenceTest
// {
// protected static long timeForEquals = 0;
//
// protected static long timeForIsIn = 0;
//
// @Autowired
// protected IChildService childService;
//
// @Test
// public void testTimeForEquals() throws Exception
// {
// long start = System.currentTimeMillis();
// for (int a = 100; a-- > 0;)
// {
// childService.searchForParentWithEquals(10001);
// }
// timeForEquals = System.currentTimeMillis() - start;
// checkTimes();
// }
//
// @Test
// public void testTimeForIsIn() throws Exception
// {
// long start = System.currentTimeMillis();
// for (int a = 100; a-- > 0;)
// {
// childService.getForParentWithIsIn(10002);
// }
// timeForIsIn = System.currentTimeMillis() - start;
// checkTimes();
// }
//
// @Test
// public void testTimeForIsInMoreThan4000() throws Exception
// {
// int[] parentIds = new int[4005]; // Force UNION > 4000
// for (int a = parentIds.length; a-- > 0;)
// {
// parentIds[a] = 10000 + a;
// }
// long start = System.currentTimeMillis();
// childService.getForParentWithIsIn(parentIds);
// timeForIsIn = System.currentTimeMillis() - start;
// checkTimes();
// }
//
// private void checkTimes()
// {
// if (timeForEquals > 0 && timeForIsIn > 0)
// {
// if (timeForEquals < 50)
// {
// Assert.fail("Difference not significant. Use a larger number of entities.");
// }
// if (timeForIsIn > timeForEquals * 1.5)
// {
// Assert.fail("IsIn is to slow: timeForEquals = " + timeForEquals + ", timeForIsIn = " +
// timeForIsIn);
// }
// }
// }
// }
