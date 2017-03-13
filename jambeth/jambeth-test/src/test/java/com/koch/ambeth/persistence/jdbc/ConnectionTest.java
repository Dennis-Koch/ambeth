package com.koch.ambeth.persistence.jdbc;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IDatabasePool;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.AmbethIocRunner;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;

@TestPropertiesList({ @TestProperties(name = PersistenceJdbcConfigurationConstants.IntegratedConnectionPool, value = "true"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabasePoolPassivate, value = "true") })
@RunWith(AmbethIocRunner.class)
public class ConnectionTest extends AbstractInformationBusWithPersistenceTest
{
	@LogInstance
	private ILogger log;

	@Ignore
	@Test
	public void test()
	{
		// IDatabaseFactory databaseFactory = beanContext.getService(IDatabaseFactory.class);
		IDatabasePool databasePool = beanContext.getService(IDatabasePool.class);

		while (true)
		{
			long start = System.currentTimeMillis();
			IDatabase database = databasePool.acquireDatabase();

			long end = System.currentTimeMillis();
			database.flushAndRelease();

			log.info("DB Init: " + (end - start) + " ms");
		}
	}
}
