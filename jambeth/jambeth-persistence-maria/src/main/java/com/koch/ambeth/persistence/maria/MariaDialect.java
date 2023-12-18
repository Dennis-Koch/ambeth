package com.koch.ambeth.persistence.maria;

/*-
 * #%L
 * jambeth-persistence-maria
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
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.persistence.IColumnEntry;
import com.koch.ambeth.persistence.SelectPosition;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.jdbc.AbstractConnectionDialect;
import com.koch.ambeth.persistence.jdbc.ColumnEntry;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.orm.XmlDatabaseMapper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyMap;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Pattern;

public class MariaDialect extends AbstractConnectionDialect {
    public static final Pattern BIN_TABLE_NAME = Pattern.compile("BIN\\$.{22}==\\$0", Pattern.CASE_INSENSITIVE);

    public static final Pattern IDX_TABLE_NAME = Pattern.compile("DR\\$.*?\\$.", Pattern.CASE_INSENSITIVE);

    public static final String SEQUENCE_TABLE_NAME = "sequence_data";

    public static final String SEQUENCE_COLUMN_NAME = "sequence_name";

    public static final String SEQUENCE_INCREMENT_NAME = "sequence_increment";

    public static final String SEQUENCE_MIN_NAME = "sequence_min_value";

    public static final String SEQUENCE_MAX_NAME = "sequence_max_value";

    public static final String SEQUENCE_CUR_NAME = "sequence_curr_value";

    public static final String SEQUENCE_CYCLE_NAME = "sequence_cycle";

    public static final String NEXT_VAL_FUNCTION_NAME = "nextval";

    protected static final LinkedHashMap<String, Class<?>> arrayTypeNameToTypeMap = new LinkedHashMap<>(128, 0.5f);

    public static int getOptimisticLockErrorCode() {
        return 20800;
    }

    public static int getPessimisticLockErrorCode() {
        // 54 = RESOURCE BUSY acquiring with NOWAIT (pessimistic lock)
        return 54;
    }

    @Autowired
    protected ISqlBuilder sqlBuilder;

    @Property(name = PersistenceJdbcConfigurationConstants.DatabaseProtocol)
    protected String protocol;

    @Override
    protected Class<?> getDriverType() {
        return org.mariadb.jdbc.Driver.class;
    }

    @SneakyThrows
    @Override
    public List<IMap<String, String>> getExportedKeys(Connection connection, String[] schemaNames) {
        PreparedStatement pstm = null;
        ResultSet allForeignKeysRS = null;
        try {
            ArrayList<IMap<String, String>> allForeignKeys = new ArrayList<>();

            StringBuilder sb = new StringBuilder(
                    "SELECT * FROM information_schema.TABLE_CONSTRAINTS WHERE information_schema.TABLE_CONSTRAINTS.CONSTRAINT_TYPE = 'FOREIGN KEY' AND information_schema.TABLE_CONSTRAINTS" +
                            ".TABLE_SCHEMA = 'myschema' AND information_schema.TABLE_CONSTRAINTS.TABLE_NAME IN (");
            for (int a = 0, size = schemaNames.length; a < size; a++) {
                if (a > 0) {
                    sb.append(',');
                }
                sb.append('?');
            }
            sb.append(')');

            pstm = connection.prepareStatement(sb.toString());
            for (int a = 0, size = schemaNames.length; a < size; a++) {
                pstm.setObject(a + 1, schemaNames[a]);
            }
            allForeignKeysRS = pstm.executeQuery();

            while (allForeignKeysRS.next()) {
                HashMap<String, String> foreignKey = new HashMap<>();

                foreignKey.put("OWNER", allForeignKeysRS.getString("OWNER"));
                foreignKey.put("CONSTRAINT_NAME", allForeignKeysRS.getString("CONSTRAINT_NAME"));
                foreignKey.put("FKTABLE_NAME", allForeignKeysRS.getString("TABLE_NAME"));
                foreignKey.put("FKCOLUMN_NAME", allForeignKeysRS.getString("COLUMN_NAME"));
                foreignKey.put("PKTABLE_NAME", allForeignKeysRS.getString("REF_TABLE_NAME"));
                foreignKey.put("PKCOLUMN_NAME", allForeignKeysRS.getString("REF_COLUMN_NAME"));

                allForeignKeys.add(foreignKey);
            }
            return allForeignKeys;
        } finally {
            JdbcUtil.close(pstm, allForeignKeysRS);
        }
    }

    @Override
    public ILinkedMap<String, List<String>> getFulltextIndexes(Connection connection, String schemaName) {
        return EmptyMap.emptyMap();
    }

    @Override
    public boolean isSystemTable(String tableName) {
        return false;
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint, Connection connection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getResourceBusyErrorCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PersistenceException createPersistenceException(SQLException e, String relatedSql) {
        int errorCode = e.getErrorCode();
        if (errorCode == getOptimisticLockErrorCode()) {
            OptimisticLockException ex = new OptimisticLockException(relatedSql, e);
            ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
            return ex;
        }
        return null;
    }

    @SneakyThrows
    @Override
    public ResultSet getIndexInfo(Connection connection, String schemaName, String tableName, boolean unique) {
        return connection.getMetaData().getIndexInfo(null, schemaName, tableName, unique, true);
    }

    @Override
    public Class<?> getComponentTypeByFieldTypeName(String fieldTypeName) {
        if (fieldTypeName == null) {
            return null;
        }
        return arrayTypeNameToTypeMap.get(fieldTypeName);
    }

    @Override
    public String getFieldTypeNameByComponentType(Class<?> componentType) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @SneakyThrows
    @Override
    public List<String> getAllFullqualifiedTableNames(Connection connection, String... schemaNames) {
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            StringBuilder sb = new StringBuilder("SELECT CONCAT(table_schema,'.',table_name) AS fq_table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_type='BASE TABLE' AND table_schema IN (");
            for (int a = 0, size = schemaNames.length; a < size; a++) {
                if (a > 0) {
                    sb.append(',');
                }
                sb.append('?');
            }
            sb.append(')');

            pstm = connection.prepareStatement(sb.toString());
            for (int a = 0, size = schemaNames.length; a < size; a++) {
                pstm.setObject(a + 1, schemaNames[a]);
            }
            rs = pstm.executeQuery();
            ArrayList<String> tableNames = new ArrayList<>();
            while (rs.next()) {
                String fqTableName = rs.getString("fq_table_name");
                String softTableName = XmlDatabaseMapper.splitSchemaAndName(fqTableName)[1];
                if (softTableName.equals(MariaDialect.SEQUENCE_TABLE_NAME)) {
                    continue;
                }
                tableNames.add(fqTableName);
            }
            return tableNames;
        } finally {
            JdbcUtil.close(pstm, rs);
        }
    }

    @SneakyThrows
    @Override
    public List<String> getAllFullqualifiedViews(Connection connection, String... schemaNames) {
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            StringBuilder sb = new StringBuilder("SELECT CONCAT(table_schema,'.',table_name) AS fq_table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_type='VIEW' AND table_schema IN (");
            for (int a = 0, size = schemaNames.length; a < size; a++) {
                if (a > 0) {
                    sb.append(',');
                }
                sb.append('?');
            }
            sb.append(')');

            pstm = connection.prepareStatement(sb.toString());
            for (int a = 0, size = schemaNames.length; a < size; a++) {
                pstm.setObject(a + 1, schemaNames[a]);
            }
            rs = pstm.executeQuery();
            ArrayList<String> tableNames = new ArrayList<>();
            while (rs.next()) {
                tableNames.add(rs.getString("fq_table_name"));
            }
            return tableNames;
        } finally {
            JdbcUtil.close(pstm, rs);
        }
    }

    @SneakyThrows
    @Override
    public List<IColumnEntry> getAllFieldsOfTable(Connection connection, String fqTableName) {
        String[] names = sqlBuilder.getSchemaAndTableName(fqTableName);
        ResultSet tableColumnsRS = connection.getMetaData().getColumns(null, names[0], names[1], null);
        try {
            ArrayList<IColumnEntry> columns = new ArrayList<>();
            while (tableColumnsRS.next()) {
                String fieldName = tableColumnsRS.getString("COLUMN_NAME");
                int columnIndex = tableColumnsRS.getInt("ORDINAL_POSITION");
                int typeIndex = tableColumnsRS.getInt("DATA_TYPE");

                String typeName = tableColumnsRS.getString("TYPE_NAME");

                String isNullable = tableColumnsRS.getString("IS_NULLABLE");
                boolean nullable = "YES".equalsIgnoreCase(isNullable);

                int scale = tableColumnsRS.getInt("COLUMN_SIZE");
                int digits = tableColumnsRS.getInt("DECIMAL_DIGITS");
                int radix = tableColumnsRS.getInt("NUM_PREC_RADIX");

                Class<?> javaType = JdbcUtil.getJavaTypeFromJdbcType(typeIndex, scale, digits);

                ColumnEntry entry = new ColumnEntry(fieldName, columnIndex, javaType, typeName, nullable, radix, true);
                columns.add(entry);
            }
            return columns;
        } finally {
            JdbcUtil.close(tableColumnsRS);
        }
    }

    @Override
    protected String buildDeferrableForeignKeyConstraintsSelectSQL(String[] schemaNames) {
        return null;
    }

    @SneakyThrows
    @Override
    public List<String> getAllFullqualifiedSequences(Connection connection, String... schemaNames) {
        Statement stm = null;
        ResultSet rs = null;
        try {
            stm = connection.createStatement();
            ArrayList<String> seqNames = new ArrayList<>();

            for (String schemaName : schemaNames) {
                try {
                    rs = stm.executeQuery("SELECT " + escapeName(SEQUENCE_COLUMN_NAME) + " AS seq_name FROM " + escapeSchemaAndSymbolName(schemaName, SEQUENCE_TABLE_NAME));
                    while (rs.next()) {
                        String seqName = rs.getString("seq_name");
                        seqNames.add(schemaName + '.' + seqName);
                    }
                } finally {
                    JdbcUtil.close(rs);
                }
            }
            return seqNames;
        } finally {
            JdbcUtil.close(stm, rs);
        }
    }

    @Override
    protected void handleRow(String schemaName, String tableName, String constraintName, ArrayList<String> disableConstraintsSQL, ArrayList<String> enableConstraintsSQL) {
        throw new UnsupportedOperationException();
    }

    @SneakyThrows
    @Override
    protected ConnectionKeyValue preProcessConnectionIntern(Connection connection, String[] schemaNames, boolean forcePreProcessing) {
        Statement stm = connection.createStatement();
        try {
            stm.execute("USE " + schemaNames[0]);
        } finally {
            JdbcUtil.close(stm);
        }
        return super.preProcessConnectionIntern(connection, schemaNames, forcePreProcessing);
    }

    @Override
    public String prepareCommand(String sqlCommand) {
        sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *1 *, *0 *\\)", " BOOLEAN");
        sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *3 *, *0 *\\)", " SMALLINT");
        sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *5 *, *0 *\\)", " INT");
        sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *9 *, *0 *\\)", " INT");
        sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *10 *, *0 *\\)", " BIGINT");
        sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *12 *, *0 *\\)", " BIGINT");
        sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *18 *, *0 *\\)", " BIGINT");
        sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *\\* *, *0 *\\)", " BIGINT");
        // sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER", " REAL");
        //
        sqlCommand = prepareCommandInternWithGroup(sqlCommand, " VARCHAR2 *\\( *(\\d+)(?: +BYTE| +CHAR)? *\\)", " VARCHAR(\\2)");
        //
        // sqlCommand = prepareCommandInternWithGroup(sqlCommand, " PRIMARY KEY (\\([^\\)]+\\)) USING
        // INDEX", " PRIMARY KEY \\2");

        // CREATE SEQUENCE "PASSWORD_SEQ" MINVALUE 1 MAXVALUE 999999999999999999 INCREMENT BY 1 START
        // WITH 10000 CACHE 20 NOORDER NOCYCLE ;
        sqlCommand = prepareCommandInternWithGroup(sqlCommand, " *CREATE +SEQUENCE +\"?([A-Za-z0-9_]+)\"? +MINVALUE +(\\d+) +MAXVALUE +(\\d+) +INCREMENT +BY +(\\d+) +START +WITH +(\\d+).*",
                "INSERT INTO " + escapeName(SEQUENCE_TABLE_NAME) + " (" + escapeName(SEQUENCE_COLUMN_NAME)//
                        + "," + escapeName(SEQUENCE_MIN_NAME)//
                        + "," + escapeName(SEQUENCE_MAX_NAME)//
                        + "," + escapeName(SEQUENCE_INCREMENT_NAME)//
                        + "," + escapeName(SEQUENCE_CUR_NAME) + ") VALUE ('\\2',\\3,\\4,\\5,\\6)");

        // CONSTRAINT "MODULE_PK" PRIMARY KEY ("ID") USING INDEX
        sqlCommand = prepareCommandInternWithGroup(sqlCommand, " *CONSTRAINT +\"?([A-Za-z0-9_]+)\"? +PRIMARY +KEY +\\(\"?([A-Za-z0-9_]+)\"\\) USING INDEX",
                " CONSTRAINT " + escapeName("\\2") + " PRIMARY KEY (" + escapeName("\\3") + ")");

        sqlCommand = prepareCommandInternWithGroup(sqlCommand, "( *)\"([A-Za-z0-9_]+)\"", "\\2" + escapeName("\\3"));

        return sqlCommand;
    }

    @Override
    public SelectPosition getLimitPosition() {
        return SelectPosition.AS_WHERE_CLAUSE;
    }

    @Override
    public String getEscapeLiteral() {
        return "`";
    }

    @Override
    public String buildClearTableSQL(String tableName) {
        return "DELETE FROM " + escapeName(tableName);
    }

    @Override
    public String toDefaultCase(String identifier) {
        return identifier.toLowerCase();
    }

    @Override
    public String getSelectForUpdateFragment() {
        return " FOR UPDATE";
    }
}
