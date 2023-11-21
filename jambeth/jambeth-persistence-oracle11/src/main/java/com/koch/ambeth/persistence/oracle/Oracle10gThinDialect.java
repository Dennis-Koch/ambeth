package com.koch.ambeth.persistence.oracle;

/*-
 * #%L
 * jambeth-persistence-oracle11
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
import java.sql.SQLRecoverableException;
import java.sql.Statement;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.persistence.SQLState;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.util.exception.MaskingRuntimeException;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import lombok.SneakyThrows;

public class Oracle10gThinDialect extends Oracle10gDialect {
	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseAutoCleanupRecycleBin, defaultValue = "false")
	protected boolean autoCleanupRecycleBin;

	@Override
	public void preProcessConnection(Connection connection, String[] schemaNames,
			boolean forcePreProcessing) {
		super.preProcessConnection(connection, schemaNames, forcePreProcessing);

		if (autoCleanupRecycleBin) {
			try {
				Statement stm = connection.createStatement();
				try {
					// Deletes those ugly recyclebin tables
					stm.execute("PURGE RECYCLEBIN");
				}
				finally {
					JdbcUtil.close(stm);
				}
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@SneakyThrows
	@Override
	public void commit(Connection connection) {
		connection.commit();
	}

	@SneakyThrows
	@Override
	public void rollback(Connection connection) {
		try {
			connection.rollback();
		}
		catch (SQLException e) {
			if (SQLState.CONNECTION_NOT_OPEN.getXopen().equals(e.getSQLState())) // Closed connection
			{
				return;
			}
			throw e;
		}
		catch (MaskingRuntimeException e) {
			if (e.getCause() instanceof SQLRecoverableException) {
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

	@SneakyThrows
	@Override
	public ResultSet getIndexInfo(Connection connection, String schemaName, String tableName,
			boolean unique) {
		return connection.getMetaData().getIndexInfo(null, schemaName, tableName, unique, true);
	}
}
