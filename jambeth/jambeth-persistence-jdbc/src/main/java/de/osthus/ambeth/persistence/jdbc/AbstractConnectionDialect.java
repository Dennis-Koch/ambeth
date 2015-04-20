package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.ReadOnlyList;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.IConnectionKeyHandle;
import de.osthus.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;

public abstract class AbstractConnectionDialect implements IConnectionDialect, IInitializingBean, IDisposableBean
{
	public static class ConnectionKeyValue
	{
		protected String[] disableConstraintsSQL;

		protected String[] enableConstraintsSQL;

		public ConnectionKeyValue(String[] disableConstraintsSQL, String[] enableConstraintsSQL)
		{
			super();
			this.disableConstraintsSQL = disableConstraintsSQL;
			this.enableConstraintsSQL = enableConstraintsSQL;
		}

		public String[] getDisableConstraintsSQL()
		{
			return disableConstraintsSQL;
		}

		public String[] getEnableConstraintsSQL()
		{
			return enableConstraintsSQL;
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IDatabaseConnectionUrlProvider databaseConnectionUrlProvider;

	@Autowired
	protected IProperties props;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired(optional = true)
	protected ITransactionState transactionState;

	@Property(name = PersistenceConfigurationConstants.ExternalTransactionManager, defaultValue = "false")
	protected boolean externalTransactionManager;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName)
	protected String schemaName;

	protected String[] schemaNames;

	protected Driver driverRegisteredExplicitly;

	protected final WeakHashMap<IConnectionKeyHandle, ConnectionKeyValue> connectionToConstraintSqlMap = new WeakHashMap<IConnectionKeyHandle, ConnectionKeyValue>();

	protected final Lock readLock, writeLock;

	public AbstractConnectionDialect()
	{
		ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		readLock = rwLock.readLock();
		writeLock = rwLock.writeLock();
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		registerDriverIfNeeded();

		schemaNames = toDefaultCase(schemaName).split("[:;]");
	}

	protected abstract Class<?> getDriverType();

	@Override
	public String toDefaultCase(String identifier)
	{
		return identifier.toUpperCase(); // uppercase is the SQL standard
	}

	protected void registerDriverIfNeeded()
	{
		Class<?> databaseDriver = getDriverType();
		if (databaseDriver == null || !Driver.class.isAssignableFrom(databaseDriver))
		{
			return;
		}
		try
		{
			try
			{
				DriverManager.getDriver(databaseConnectionUrlProvider.getConnectionUrl());
			}
			catch (SQLException e)
			{
				if (!"08001".equals(e.getSQLState()))
				{
					throw e;
				}
				driverRegisteredExplicitly = (Driver) databaseDriver.newInstance();
				DriverManager.registerDriver(driverRegisteredExplicitly);
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void destroy() throws Throwable
	{
		if (driverRegisteredExplicitly != null)
		{
			DriverManager.deregisterDriver(driverRegisteredExplicitly);
			driverRegisteredExplicitly = null;
		}
	}

	@Override
	public int getMaxInClauseBatchThreshold()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public void preProcessConnection(Connection connection, String[] schemaNames, boolean forcePreProcessing)
	{
		try
		{
			ConnectionKeyValue connectionKeyValue = null;
			IConnectionKeyHandle connectionKeyHandle = null;
			Lock writeLock = this.writeLock;

			if (connection.isWrapperFor(IConnectionKeyHandle.class))
			{
				connectionKeyHandle = connection.unwrap(IConnectionKeyHandle.class);
				writeLock.lock();
				try
				{
					// WeakHashMaps have ALWAYS to be exclusively locked even if they SEEM to be only read-accessed
					connectionKeyValue = connectionToConstraintSqlMap.get(connectionKeyHandle);
				}
				finally
				{
					writeLock.unlock();
				}
			}
			if (forcePreProcessing || connectionKeyValue == null)
			{
				if (connectionKeyHandle == null)
				{
					throw new IllegalStateException("Should never happen");
				}
				connectionKeyValue = preProcessConnectionIntern(connection, schemaNames, forcePreProcessing);
				writeLock.lock();
				try
				{
					connectionToConstraintSqlMap.put(connectionKeyHandle, connectionKeyValue);
				}
				finally
				{
					writeLock.unlock();
				}
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected ConnectionKeyValue preProcessConnectionIntern(Connection connection, String[] schemaNames, boolean forcePreProcessing) throws SQLException
	{
		return scanForUndeferredDeferrableConstraints(connection, schemaNames);
	}

	@Override
	public IList<String> disableConstraints(Connection connection, String... schemaNames)
	{
		try
		{
			ConnectionKeyValue connectionKeyValue;
			IConnectionKeyHandle connectionKeyHandle = null;

			if (connection.isWrapperFor(IConnectionKeyHandle.class))
			{
				connectionKeyHandle = connection.unwrap(IConnectionKeyHandle.class);
				Lock writeLock = this.writeLock;
				writeLock.lock();
				try
				{
					// WeakHashMaps have ALWAYS to be exclusively locked even if they SEEM to be only read-accessed
					connectionKeyValue = connectionToConstraintSqlMap.get(connectionKeyHandle);
				}
				finally
				{
					writeLock.unlock();
				}
			}
			else
			{
				throw new IllegalStateException("Connection is not a wrapper for " + IConnectionKeyHandle.class.getName());
			}
			String[] constraintSql = connectionKeyValue.getDisableConstraintsSQL();

			if (constraintSql.length > 0)
			{
				Statement stm = connection.createStatement();
				try
				{
					for (int a = 0, size = constraintSql.length; a < size; a++)
					{
						stm.addBatch(constraintSql[a]);
					}
					stm.executeBatch();
				}
				finally
				{
					JdbcUtil.close(stm);
				}
			}
			return new ReadOnlyList<String>(connectionKeyValue.getEnableConstraintsSQL());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void enableConstraints(Connection connection, IList<String> enableConstraintsSQL)
	{
		if (enableConstraintsSQL == null || enableConstraintsSQL.isEmpty())
		{
			return;
		}
		Statement stmt = null;
		try
		{
			stmt = connection.createStatement();
			for (int i = enableConstraintsSQL.size(); i-- > 0;)
			{
				stmt.addBatch(enableConstraintsSQL.get(i));
			}
			stmt.executeBatch();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(stmt);
		}
	}

	@Override
	public boolean useVersionOnOptimisticUpdate()
	{
		return false;
	}

	@Override
	public void commit(Connection connection) throws SQLException
	{
		Boolean active = transactionState != null ? transactionState.isExternalTransactionManagerActive() : null;
		if (active == null)
		{
			active = Boolean.valueOf(externalTransactionManager);
		}
		if (active.booleanValue())
		{
			// No Action!
			// Transactions are externally managed.
		}
		else
		{
			connection.commit();
		}
	}

	@Override
	public void rollback(Connection connection) throws SQLException
	{
		Boolean active = transactionState != null ? transactionState.isExternalTransactionManagerActive() : null;
		if (active == null)
		{
			active = Boolean.valueOf(externalTransactionManager);
		}
		if (active.booleanValue())
		{
			// No Action!
			// Transactions are externally managed.
		}
		else
		{
			connection.rollback();
		}
	}

	protected void printResultSet(ResultSet rs) throws SQLException
	{
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		for (int a = 0, size = columnCount; a < size; a++)
		{
			System.out.print(metaData.getColumnLabel(a + 1));
			System.out.print("\t\t");
		}
		System.out.println("\t\t");
		while (rs.next())
		{
			for (int a = 0, size = columnCount; a < size; a++)
			{
				System.out.print(rs.getObject(a + 1));
				System.out.print("\t\t");
			}
			System.out.println();
		}
	}

	protected ConnectionKeyValue scanForUndeferredDeferrableConstraints(Connection connection, String[] schemaNames) throws SQLException
	{
		Statement stm = connection.createStatement();
		try
		{
			ArrayList<String> disableConstraintsSQL = new ArrayList<String>();
			ArrayList<String> enableConstraintsSQL = new ArrayList<String>();
			String sql = buildDeferrableForeignKeyConstraintsSelectSQL(schemaNames);
			if (sql != null)
			{
				ResultSet rs = stm.executeQuery(sql);
				while (rs.next())
				{
					String schemaName = rs.getString("OWNER");
					String tableName = rs.getString("TABLE_NAME");
					String constraintName = rs.getString("CONSTRAINT_NAME");

					handleRow(schemaName, tableName, constraintName, disableConstraintsSQL, enableConstraintsSQL);
				}
			}
			String[] disableConstraintsArray = disableConstraintsSQL.toArray(new String[disableConstraintsSQL.size()]);
			String[] enabledConstraintsArray = enableConstraintsSQL.toArray(new String[enableConstraintsSQL.size()]);
			ConnectionKeyValue connectionKeyValue = new ConnectionKeyValue(disableConstraintsArray, enabledConstraintsArray);

			return connectionKeyValue;
		}
		finally
		{
			JdbcUtil.close(stm);
		}
	}

	protected abstract String buildDeferrableForeignKeyConstraintsSelectSQL(String[] schemaNames);

	protected abstract void handleRow(String schemaName, String tableName, String constraintName, ArrayList<String> disableConstraintsSQL,
			ArrayList<String> enableConstraintsSQL);

	protected String buildSchemaInClause(final String... schemaNames)
	{
		StringBuilder sb = new StringBuilder();
		buildSchemaInClause(sb, schemaNames);
		return sb.toString();
	}

	protected void buildSchemaInClause(final StringBuilder sb, final String... schemaNames)
	{
		sb.append(" IN (");
		boolean first = true;
		for (int a = schemaNames.length; a-- > 0;)
		{
			if (!first)
			{
				sb.append(',');
			}
			sb.append('\'').append(schemaNames[a]).append('\'');
			first = false;
		}
		sb.append(')');
	}
}
