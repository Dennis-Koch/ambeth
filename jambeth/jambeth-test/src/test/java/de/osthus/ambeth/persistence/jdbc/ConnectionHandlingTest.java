package de.osthus.ambeth.persistence.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;

import org.junit.Test;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.database.DatabaseCallback;
import de.osthus.ambeth.exception.MaskingRuntimeException;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.testutil.TestRebuildContext;

@TestPropertiesList({ @TestProperties(name = PersistenceJdbcConfigurationConstants.IntegratedConnectionPool, value = "true"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseBehaviourStrict, value = "true") })
@TestRebuildContext
public class ConnectionHandlingTest extends AbstractInformationBusWithPersistenceTest
{
	// Normal behavior. No Exception and only one created connection in log.
	@Test
	public void testClosedConnection_1()
	{
		transaction.processAndCommit(new DatabaseCallback()
		{
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
			{
				// Opening and closing a transaction
			}
		});
		transaction.processAndCommit(new DatabaseCallback()
		{
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
			{
				// Just trying to start a transaction
			}
		});
	}

	// Calling close() on connection proxy.
	@Test
	public void testClosedConnection_2()
	{
		final Connection connection = beanContext.getService(Connection.class);
		try
		{
			transaction.processAndCommit(new DatabaseCallback()
			{
				@Override
				public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
				{
					try
					{
						connection.close();
					}
					catch (SQLException e)
					{
						throw RuntimeExceptionUtil.mask(e);
					}
				}
			});
		}
		catch (Exception e)
		{
			assertTrue("Is instance of '" + e.getClass().getName() + "'", e instanceof MaskingRuntimeException);
			Throwable e2 = e.getCause();
			assertTrue("Is instance of '" + e.getClass().getName() + "'", e2 instanceof SQLException);
			assertEquals("CONNECTION_NOT_OPEN", e2.getMessage());
			// Ignore to simulate other thread for next transaction.
		}
		transaction.processAndCommit(new DatabaseCallback()
		{
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
			{
				// Just trying to start a transaction
			}
		});
	}

	// Calling close() on unwrapped connection.
	@Test
	public void testClosedConnection_3()
	{
		final Connection connection = beanContext.getService(Connection.class);
		try
		{
			transaction.processAndCommit(new DatabaseCallback()
			{
				@Override
				public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
				{
					try
					{
						Connection conn = connection.unwrap(Connection.class);
						conn.close();
					}
					catch (SQLException e)
					{
						throw RuntimeExceptionUtil.mask(e);
					}
				}
			});
		}
		catch (Exception e)
		{
			assertTrue("Is instance of '" + e.getClass().getName() + "'", e instanceof MaskingRuntimeException);
			Throwable e2 = e.getCause();
			assertTrue("Is instance of '" + e2.getClass().getName() + "'", e2 instanceof SQLRecoverableException);
			assertTrue("Getrennte Verbindung".equals(e2.getMessage()) || "Closed Connection".equals(e2.getMessage()));
			// Ignore to simulate other thread for next transaction.
		}
		transaction.processAndCommit(new DatabaseCallback()
		{
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
			{
				// Just trying to start a transaction
			}
		});
	}
}
