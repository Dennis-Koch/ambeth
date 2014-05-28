package de.osthus.ambeth.persistence.jdbc;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.osthus.ambeth.IDatabasePool;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.AmbethIocRunner;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

@TestPropertiesList({ @TestProperties(name = PersistenceJdbcConfigurationConstants.IntegratedConnectionPool, value = "true"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabasePoolPassivate, value = "true") })
@RunWith(AmbethIocRunner.class)
public class ConnectionTest extends AbstractPersistenceTest
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
