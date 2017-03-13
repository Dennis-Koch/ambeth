package com.koch.ambeth.persistence.h2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.api.Trigger;

import com.koch.ambeth.persistence.jdbc.JdbcUtil;

public class OptimisticLockTrigger implements Trigger
{
	protected String tableName;

	protected int versionIndex = -1;

	@Override
	public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) throws SQLException
	{
		this.tableName = tableName;
		ResultSet rs = conn.getMetaData().getColumns(null, schemaName, tableName, null);
		int index = 0;
		try
		{
			while (rs.next())
			{
				String columnName = rs.getString("COLUMN_NAME");
				if ("VERSION".equals(columnName))
				{
					break;
				}
				index++;
			}
		}
		finally
		{
			JdbcUtil.close(rs);
		}
		if (index == -1)
		{
			throw new IllegalStateException("Table '" + tableName + "' has no column with name 'VERSION'");
		}
		this.versionIndex = index;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException
	{
		Object oldVersion = oldRow[versionIndex];
		Object newVersion = newRow[versionIndex];
		if (((Comparable<Object>) newVersion).compareTo(oldVersion) > 0)
		{
			return;
		}
		throw new SQLException("Optimistic Lock Exception", "", H2Dialect.getOptimisticLockErrorCode());
	}

	@Override
	public void close() throws SQLException
	{
	}

	@Override
	public void remove() throws SQLException
	{
	}
}
