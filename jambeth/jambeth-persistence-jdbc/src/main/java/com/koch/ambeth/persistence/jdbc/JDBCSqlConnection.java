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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.persistence.ArrayQueryItem;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.ILinkMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.sql.IResultSet;
import com.koch.ambeth.persistence.sql.SqlConnection;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.sensor.ISensor;
import com.koch.ambeth.util.sensor.Sensor;
import jakarta.persistence.PersistenceException;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class JDBCSqlConnection extends SqlConnection {
    @Autowired
    protected Connection connection;

    @Autowired
    protected IConnectionExtension connectionExtension;

    @Autowired
    protected IDatabaseMetaData databaseMetaData;

    @Sensor(name = JDBCResultSet.SENSOR_NAME)
    protected ISensor jdbcResultSetSensor;

    @Override
    public void directSql(String sql) {
        Statement stm = null;
        try {
            stm = connection.createStatement();
            stm.execute(sql);
        } catch (SQLException e) {
            throw RuntimeExceptionUtil.mask(e);
        } finally {
            JdbcUtil.close(stm);
        }
    }

    @Override
    protected void queueSqlExecute(String sql, List<Object> parameters) {
        Statement stm = null;
        try {
            if (parameters != null) {
                PreparedStatement pstm = connection.prepareStatement(sql);
                stm = pstm;
                for (int index = 0, size = parameters.size(); index < size; index++) {
                    Object value = parameters.get(index);
                    pstm.setObject(index + 1, value);
                }
                pstm.execute();
            } else {
                stm = connection.createStatement();
                stm.execute(sql);
            }
        } catch (SQLException e) {
            throw RuntimeExceptionUtil.mask(e);
        } finally {
            JdbcUtil.close(stm);
        }
    }

    @Override
    protected int[] queueSqlExecute(String[] sql) {
        Statement stm = null;
        try {
            stm = connection.createStatement();
            for (int i = sql.length; i-- > 0; ) {
                stm.addBatch(sql[i]);
            }
            return stm.executeBatch();
        } catch (SQLException e) {
            throw RuntimeExceptionUtil.mask(e);
        } finally {
            JdbcUtil.close(stm);
        }
    }

    @Override
    protected IResultSet sqlSelect(String sql, List<Object> parameters) {
        ResultSet resultSet = null;
        Statement stm = null;
        boolean success = false;
        try {
            if (parameters != null) {
                IList<Object> arraysToDispose = null;
                var connectionExtension = this.connectionExtension;
                try {
                    var pstm = connection.prepareStatement(sql);
                    stm = pstm;
                    for (int index = 0, size = parameters.size(); index < size; index++) {
                        var value = parameters.get(index);
                        if (value instanceof ArrayQueryItem) {
                            var aqi = (ArrayQueryItem) value;
                            value = connectionExtension.createJDBCArray(aqi.getFieldType(), aqi.getValues());

                            if (arraysToDispose == null) {
                                arraysToDispose = new ArrayList<>();
                            }
                            arraysToDispose.add(value);
                        } else if (value != null && value.getClass().isEnum()) {
                            value = value.toString();
                        }
                        pstm.setObject(index + 1, value);
                    }
                    resultSet = pstm.executeQuery();
                } finally {
                    if (arraysToDispose != null) {
                        for (int a = arraysToDispose.size(); a-- > 0; ) {
                            disposeArray(arraysToDispose.get(a));
                        }
                    }
                }
            } else {
                stm = connection.createStatement();

                resultSet = stm.executeQuery(sql);
            }
            success = true;
        } catch (PersistenceException e) {
            throw e;
        } catch (Throwable e) {
            throw RuntimeExceptionUtil.mask(e, "Error occured while executing sql: " + sql);
        } finally {
            if (!success) {
                JdbcUtil.close(stm, resultSet);
            }
        }
        var jdbcResultSet = new JDBCResultSet();
        jdbcResultSet.setResultSet(resultSet);
        jdbcResultSet.setSensor(jdbcResultSetSensor);
        jdbcResultSet.setSql(sql);
        jdbcResultSet.afterPropertiesSet();
        return jdbcResultSet;
    }

    @Override
    protected Object createArray(String tableName, String idFieldName, List<?> ids) {
        Class<?> fieldType = null;
        ITableMetaData table = databaseMetaData.getTableByName(tableName);
        if (table != null) {
            fieldType = table.getFieldByName(idFieldName).getFieldType();
        } else {
            ILinkMetaData link = databaseMetaData.getLinkByName(tableName);
            if (link.getFromField().getName().equals(idFieldName)) {
                fieldType = link.getFromField().getFieldType();
            } else if (link.getToField().getName().equals(idFieldName)) {
                fieldType = link.getToField().getFieldType();
            }
        }
        if (fieldType == null) {
            throw new IllegalStateException("Must never happen");
        }
        return connectionExtension.createJDBCArray(fieldType, ids.toArray());
    }

    @Override
    protected void disposeArray(Object array) {
        if (array == null) {
            return;
        }
        try {
            ((Array) array).free();
        } catch (SQLException e) {
            // Intended blank
        }
    }

}
