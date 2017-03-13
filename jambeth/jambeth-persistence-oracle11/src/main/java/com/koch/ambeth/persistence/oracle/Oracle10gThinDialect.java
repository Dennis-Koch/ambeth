package com.koch.ambeth.persistence.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.Statement;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.SQLState;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.util.exception.MaskingRuntimeException;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

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
