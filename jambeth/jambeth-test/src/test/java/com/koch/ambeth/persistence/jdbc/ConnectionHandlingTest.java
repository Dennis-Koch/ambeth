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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;

import javax.persistence.PersistenceException;

import org.junit.Test;

import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.DatabaseCallback;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.TestRebuildContext;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

@TestPropertiesList({
		@TestProperties(name = PersistenceJdbcConfigurationConstants.IntegratedConnectionPool,
				value = "true"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseBehaviourStrict,
				value = "true")})
@TestRebuildContext
public class ConnectionHandlingTest extends AbstractInformationBusWithPersistenceTest {
	// Normal behavior. No Exception and only one created connection in log.
	@Test
	public void testClosedConnection_1() {
		transaction.processAndCommit(new DatabaseCallback() {
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) {
				// Opening and closing a transaction
			}
		});
		transaction.processAndCommit(new DatabaseCallback() {
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) {
				// Just trying to start a transaction
			}
		});
	}

	// Calling close() on connection proxy.
	@Test
	public void testClosedConnection_2() {
		final Connection connection = beanContext.getService(Connection.class);
		try {
			transaction.processAndCommit(new DatabaseCallback() {
				@Override
				public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) {
					try {
						connection.close();
					}
					catch (SQLException e) {
						throw RuntimeExceptionUtil.mask(e);
					}
				}
			});
		}
		catch (Exception e) {
			assertTrue("Is instance of '" + e.getClass().getName() + "'",
					e instanceof PersistenceException);
			Throwable e2 = e.getCause();
			assertTrue("Is instance of '" + e.getClass().getName() + "'", e2 instanceof SQLException);
			assertEquals("CONNECTION_NOT_OPEN", e2.getMessage());
			// Ignore to simulate other thread for next transaction.
		}
		// DP: Please review, this does not make sense to me. The connection is still closed.

		// transaction.processAndCommit(new DatabaseCallback()
		// {
		// @Override
		// public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
		// {
		// // Just trying to start a transaction
		// }
		// });
	}

	// Calling close() on unwrapped connection.
	@Test
	public void testClosedConnection_3() {
		final Connection connection = beanContext.getService(Connection.class);
		try {
			transaction.processAndCommit(new DatabaseCallback() {
				@Override
				public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) {
					try {
						Connection conn = connection.unwrap(Connection.class);
						conn.close();
					}
					catch (SQLException e) {
						throw RuntimeExceptionUtil.mask(e);
					}
				}
			});
		}
		catch (Exception e) {
			assertTrue("Is instance of '" + e.getClass().getName() + "'",
					e instanceof PersistenceException);
			Throwable e2 = e.getCause();
			assertTrue("Is instance of '" + e2.getClass().getName() + "'",
					e2 instanceof SQLRecoverableException);
			assertTrue("Getrennte Verbindung".equals(e2.getMessage())
					|| "Closed Connection".equals(e2.getMessage()));
			// Ignore to simulate other thread for next transaction.
		}
		// DP: Please review, this does not make sense to me. The connection is still closed.

		// transaction.processAndCommit(new DatabaseCallback()
		// {
		// @Override
		// public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
		// {
		// // Just trying to start a transaction
		// }
		// });
	}
}
