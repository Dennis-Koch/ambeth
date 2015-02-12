package de.osthus.ambeth.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.Statement;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.MaskingRuntimeException;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.SQLState;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;

public class Oracle10gThinDialect extends Oracle10gDialect
{

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseAutoCleanupRecycleBin, defaultValue = "false")
	protected boolean autoCleanupRecycleBin;

	@Override
	public void preProcessConnection(Connection connection, String[] schemaNames, boolean forcePreProcessing)
	{
		super.preProcessConnection(connection, schemaNames, forcePreProcessing);

		if (autoCleanupRecycleBin)
		{
			try
			{
				Statement stm = connection.createStatement();
				try
				{
					// Deletes those ugly recyclebin tables
					stm.execute("PURGE RECYCLEBIN");
				}
				finally
				{
					JdbcUtil.close(stm);
				}
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public void commit(Connection connection) throws SQLException
	{
		connection.commit();
	}

	@Override
	public void rollback(Connection connection) throws SQLException
	{
		try
		{
			connection.rollback();
		}
		catch (SQLException e)
		{
			if (SQLState.CONNECTION_NOT_OPEN.getXopen().equals(e.getSQLState())) // Closed connection
			{
				return;
			}
			throw e;
		}
		catch (MaskingRuntimeException e)
		{
			if (e.getCause() instanceof SQLRecoverableException)
			{
				SQLRecoverableException sqlEx = (SQLRecoverableException) e.getCause();
				String sqlState = sqlEx.getSQLState();
				if (SQLState.CONNECTION_NOT_OPEN.getXopen().equals(sqlState)) // Closed connection
				{
					return;
				}
			}
			throw e;
		}
	}

	@Override
	public ResultSet getIndexInfo(Connection connection, String schemaName, String tableName, boolean unique) throws SQLException
	{
		return connection.getMetaData().getIndexInfo(null, schemaName, tableName, unique, true);
	}
}
