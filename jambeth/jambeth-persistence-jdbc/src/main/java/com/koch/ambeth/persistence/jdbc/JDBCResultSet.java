package com.koch.ambeth.persistence.jdbc;

/*-
 * #%L
 * jambeth-persistence-jdbc
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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.persistence.sql.IResultSet;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.sensor.ISensor;

public class JDBCResultSet implements IResultSet, IInitializingBean, IDisposable {
	public static final String SENSOR_NAME = "com.koch.ambeth.persistence.jdbc.JDBCResultSet";

	protected ResultSet resultSet;

	protected String sql;

	protected ISensor sensor;

	protected Object[] values;

	protected boolean[] isClob;

	@Override
	public void afterPropertiesSet() {
		ParamChecker.assertNotNull(resultSet, "resultSet");
		ParamChecker.assertNotNull(sql, "sql");
		try {
			ResultSetMetaData rsMetaData = resultSet.getMetaData();
			int numberOfColumns = rsMetaData.getColumnCount();
			values = new Object[numberOfColumns];
			isClob = new boolean[numberOfColumns];

			for (int i = 0; i < numberOfColumns; i++) {
				int columnType = rsMetaData.getColumnType(i + 1);
				isClob[i] = columnType == Types.CLOB;
			}
		}
		catch (SQLException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		ISensor sensor = this.sensor;
		if (sensor != null) {
			sensor.on(sql);
		}
	}

	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public void setSensor(ISensor sensor) {
		this.sensor = sensor;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	@Override
	public void dispose() {
		if (resultSet == null) {
			return;
		}
		Statement stm = null;
		try {
			stm = resultSet.getStatement();
		}
		catch (Throwable e) {
			// Intended blank
		}
		finally {
			JdbcUtil.close(stm, resultSet);
		}
		resultSet = null;
		ISensor sensor = this.sensor;
		if (sensor != null) {
			sensor.off();
		}
	}

	@Override
	public boolean moveNext() {
		try {
			ResultSet resultSet = this.resultSet;
			if (resultSet == null) {
				return false;
			}
			Object[] values = this.values;
			if (!resultSet.next()) {
				for (int a = values.length; a-- > 0;) {
					values[a] = null;
				}
				return false;
			}
			for (int a = values.length; a-- > 0;) {
				if (!isClob[a]) {
					values[a] = resultSet.getObject(a + 1);
				}
				else {
					values[a] = resultSet.getClob(a + 1);
				}
			}
			return true;
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Object[] getCurrent() {
		return values;
	}
}
