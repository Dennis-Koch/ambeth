package com.koch.ambeth.persistence.jdbc;

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

@TestPropertiesList({
		@TestProperties(name = PersistenceJdbcConfigurationConstants.IntegratedConnectionPool,
				value = "true"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabasePoolPassivate,
				value = "true")})
@RunWith(AmbethIocRunner.class)
public class ConnectionTest extends AbstractInformationBusWithPersistenceTest {
	@LogInstance
	private ILogger log;

	@Ignore
	@Test
	public void test() {
		// IDatabaseFactory databaseFactory = beanContext.getService(IDatabaseFactory.class);
		IDatabasePool databasePool = beanContext.getService(IDatabasePool.class);

		while (true) {
			long start = System.currentTimeMillis();
			IDatabase database = databasePool.acquireDatabase();

			long end = System.currentTimeMillis();
			database.flushAndRelease();

			log.info("DB Init: " + (end - start) + " ms");
		}
	}
}
