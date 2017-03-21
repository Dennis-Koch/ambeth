package com.koch.ambeth.persistence.h2;

/*-
 * #%L
 * jambeth-persistence-h2
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
