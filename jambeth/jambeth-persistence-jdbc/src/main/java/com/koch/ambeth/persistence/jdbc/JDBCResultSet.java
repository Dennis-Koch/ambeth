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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.persistence.sql.IResultSet;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedFunction;
import com.koch.ambeth.util.sensor.ISensor;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;

public class JDBCResultSet implements IResultSet, Iterator<Object[]>, IInitializingBean {
    public static final String SENSOR_NAME = "com.koch.ambeth.persistence.jdbc.JDBCResultSet";

    protected ResultSet resultSet;

    protected String sql;

    protected ISensor sensor;

    protected Object[] values;

    protected CheckedFunction<ResultSet, Object>[] resultSetValueExtractors;

    private Boolean hasNext;

    @Override
    public void afterPropertiesSet() {
        ParamChecker.assertNotNull(resultSet, "resultSet");
        ParamChecker.assertNotNull(sql, "sql");
        try {
            var rsMetaData = resultSet.getMetaData();
            var numberOfColumns = rsMetaData.getColumnCount();
            values = new Object[numberOfColumns];
            resultSetValueExtractors = new CheckedFunction[numberOfColumns];

            for (int i = 0; i < numberOfColumns; i++) {
                var columnIndex = i + 1;
                int columnType = rsMetaData.getColumnType(columnIndex);

                if (columnType == Types.CLOB) {
                    resultSetValueExtractors[i] = resultSet -> resultSet.getClob(columnIndex);
                } else {
                    resultSetValueExtractors[i] = resultSet -> resultSet.getObject(columnIndex);
                }
            }
        } catch (SQLException e) {
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
        } catch (Throwable e) {
            // Intended blank
        } finally {
            JdbcUtil.close(stm, resultSet);
        }
        resultSet = null;
        var sensor = this.sensor;
        if (sensor != null) {
            sensor.off();
        }
    }

    @SneakyThrows
    @Override
    public boolean hasNext() {
        if (hasNext != null) {
            return hasNext.booleanValue();
        }
        var resultSet = this.resultSet;
        if (resultSet == null) {
            hasNext = Boolean.FALSE;
            return false;
        }
        var values = this.values;
        if (!resultSet.next()) {
            for (int a = values.length; a-- > 0; ) {
                values[a] = null;
            }
            hasNext = Boolean.FALSE;
            return false;
        }
        var resultSetValueExtractors = this.resultSetValueExtractors;
        for (int a = values.length; a-- > 0; ) {
            values[a] = resultSetValueExtractors[a].apply(resultSet);
        }
        hasNext = Boolean.TRUE;
        return true;
    }

    @Override
    public Object[] next() {
        if (!hasNext()) {
            return null;
        }
        hasNext = null;
        return values;
    }

    @Override
    public Iterator<Object[]> iterator() {
        return this;
    }
}
