package com.koch.ambeth.persistence.pg;

/*-
 * #%L
 * jambeth-persistence-pg
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

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.persistence.ArrayQueryItem;
import com.koch.ambeth.persistence.IColumnEntry;
import com.koch.ambeth.persistence.SQLState;
import com.koch.ambeth.persistence.SelectPosition;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.connection.IConnectionKeyHandle;
import com.koch.ambeth.persistence.jdbc.AbstractConnectionDialect;
import com.koch.ambeth.persistence.jdbc.ColumnEntry;
import com.koch.ambeth.persistence.jdbc.IConnectionExtension;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.exception.NullConstraintException;
import com.koch.ambeth.persistence.jdbc.exception.UniqueConstraintException;
import com.koch.ambeth.persistence.jdbc.sql.LimitByLimitOperator;
import com.koch.ambeth.persistence.sql.ParamsUtil;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IValueOperand;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PessimisticLockException;
import lombok.SneakyThrows;
import org.postgresql.Driver;
import org.postgresql.PGConnection;
import org.postgresql.core.BaseConnection;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.regex.Pattern;

public class PostgresDialect extends AbstractConnectionDialect {
    public static final Pattern BIN_TABLE_NAME = Pattern.compile("BIN\\$.{22}==\\$0", Pattern.CASE_INSENSITIVE);

    public static final Pattern IDX_TABLE_NAME = Pattern.compile("DR\\$.*?\\$.", Pattern.CASE_INSENSITIVE);
    protected static final LinkedHashMap<Class<?>, String[]> typeToArrayTypeNameMap = new LinkedHashMap<>(128, 0.5f);
    protected static final LinkedHashMap<String, Class<?>> arrayTypeNameToTypeMap = new LinkedHashMap<>(128, 0.5f);
    private static final Pattern pattern = Pattern.compile(" *create(?: or replace)? TYPE ([^ ]+) AS VARRAY\\(\\d+\\) OF +(.+)", Pattern.CASE_INSENSITIVE);

    static {
        typeToArrayTypeNameMap.put(Long.TYPE, new String[] { "bigint[]", "bigint" });
        typeToArrayTypeNameMap.put(Long.class, new String[] { "bigint[]", "bigint" });
        typeToArrayTypeNameMap.put(Integer.TYPE, new String[] { "integer[]", "integer" });
        typeToArrayTypeNameMap.put(Integer.class, new String[] { "integer[]", "integer" });
        typeToArrayTypeNameMap.put(Short.TYPE, new String[] { "smallint[]", "smallint" });
        typeToArrayTypeNameMap.put(Short.class, new String[] { "smallint[]", "smallint" });
        typeToArrayTypeNameMap.put(Byte.TYPE, new String[] { "smallint[]", "smallint" });
        typeToArrayTypeNameMap.put(Byte.class, new String[] { "smallint[]", "smallint" });
        typeToArrayTypeNameMap.put(Character.TYPE, new String[] { "char", "char" });
        typeToArrayTypeNameMap.put(Character.class, new String[] { "char", "char" });
        typeToArrayTypeNameMap.put(Boolean.TYPE, new String[] { "boolean[]", "boolean" });
        typeToArrayTypeNameMap.put(Boolean.class, new String[] { "boolean[]", "boolean" });
        typeToArrayTypeNameMap.put(Double.TYPE, new String[] { "double precision[]", "double precision" });
        typeToArrayTypeNameMap.put(Double.class, new String[] { "double precision[]", "double precision" });
        typeToArrayTypeNameMap.put(Float.TYPE, new String[] { "real[]", "real" });
        typeToArrayTypeNameMap.put(Float.class, new String[] { "real[]", "real" });
        typeToArrayTypeNameMap.put(String.class, new String[] { "text[]", "text" });
        typeToArrayTypeNameMap.put(BigDecimal.class, new String[] { "numeric[]", "numeric" });
        typeToArrayTypeNameMap.put(BigInteger.class, new String[] { "numeric[]", "numeric" });

        // Default behavior. This is an intended "hack" for backwards compatibility.
        typeToArrayTypeNameMap.put(Object.class, new String[] { "numeric[]", "numeric" });

        for (Entry<Class<?>, String[]> entry : typeToArrayTypeNameMap) {
            arrayTypeNameToTypeMap.putIfNotExists(entry.getValue()[0], entry.getKey());
        }
    }

    public static int getOptimisticLockErrorCode() {
        return 20800;
    }

    public static boolean isBLobColumnName(String typeName) {
        return "lo".equals(typeName);
    }

    public static boolean isCLobColumnName(String typeName) {
        return false;// "text".equals(typeName);
    }

    protected final DateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
    protected final WeakHashMap<IConnectionKeyHandle, ConnectionKeyValue> connectionToConstraintSqlMap = new WeakHashMap<>();
    protected final Lock readLock, writeLock;
    @Autowired
    protected IServiceContext serviceContext;

    @Autowired
    protected ILoggerHistory loggerHistory;

    @Autowired
    protected ISqlBuilder sqlBuilder;

    @Autowired(optional = true)
    protected ITransactionState transactionState;

    @Property(name = PersistenceConfigurationConstants.ExternalTransactionManager, defaultValue = "false")
    protected boolean externalTransactionManager;

    @Property(name = PersistenceConfigurationConstants.AutoIndexForeignKeys, defaultValue = "false")
    protected boolean autoIndexForeignKeys;

    @Property(name = PersistenceConfigurationConstants.AutoArrayTypes, defaultValue = "true")
    protected boolean autoArrayTypes;
    @LogInstance
    private ILogger log;

    public PostgresDialect() {
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        readLock = rwLock.readLock();
        writeLock = rwLock.writeLock();
    }

    @Override
    protected Class<?> getDriverType() {
        return Driver.class;
    }

    @Override
    public IOperand getRegexpLikeFunction(IOperand sourceString, IOperand pattern, IOperand matchParameter) {
        return beanContext.registerBean(PgSqlRegexpLikeOperand.class)
                          .propertyValue("SourceString", sourceString)
                          .propertyValue("Pattern", pattern)
                          .propertyValue("MatchParameter", matchParameter)
                          .finish();
    }

    @Override
    public String toDefaultCase(String identifier) {
        return identifier.toLowerCase();
    }

    @Override
    public boolean isCompactMultiValueRecommended(IList<Object> values) {
        return true;
    }

    @Override
    public void handleWithMultiValueLeftField(IAppendable querySB, Map<Object, Object> nameToValueMap, IList<Object> parameters, IList<IList<Object>> splitValues, boolean caseSensitive,
            Class<?> leftOperandFieldType) {
        if (splitValues.isEmpty()) {
            // Special scenario with EMPTY argument
            var aqi = new ArrayQueryItem(new Object[0], leftOperandFieldType);
            ParamsUtil.addParam(parameters, aqi);
            querySB.append("SELECT ");
            if (!caseSensitive) {
                querySB.append("LOWER(");
            }
            querySB.append("COLUMN_VALUE");
            if (!caseSensitive) {
                querySB.append(") COLUMN_VALUE");
            }
            querySB.append(" FROM UNNEST(ARRAY[?]) COLUMN_VALUE");
        } else {
            String placeholder;
            if (caseSensitive) {
                placeholder = "COLUMN_VALUE";
            } else {
                placeholder = "LOWER(COLUMN_VALUE) COLUMN_VALUE";
            }

            for (int a = 0, size = splitValues.size(); a < size; a++) {
                var values = splitValues.get(a);
                if (a > 0) {
                    // A union allows us to suppress the "ROWNUM" column because table(?) will already get
                    // materialized without it
                    querySB.append(" UNION ");
                }
                if (size > 1) {
                    querySB.append('(');
                }
                var aqi = new ArrayQueryItem(values.toArray(), leftOperandFieldType);
                ParamsUtil.addParam(parameters, aqi);

                querySB.append("SELECT ").append(placeholder);
                if (size < 2) {
                    // No union active
                    // querySB.append(",ROWNUM");
                }
                querySB.append(" FROM UNNEST(ARRAY[?]) COLUMN_VALUE");
                if (size > 1) {
                    querySB.append(')');
                }
            }
        }
    }

    @Override
    public int getMaxInClauseBatchThreshold() {
        return Integer.MAX_VALUE;
    }

    @SneakyThrows
    @Override
    public Blob createBlob(Connection connection) {
        var pgConnection = connection.unwrap(PGConnection.class);
        long oid = pgConnection.getLargeObjectAPI().createLO();
        return new PostgresBlob(pgConnection, oid);
    }

    @SneakyThrows
    @Override
    public Clob createClob(Connection connection) {
        var pgConnection = connection.unwrap(PGConnection.class);
        long oid = pgConnection.getLargeObjectAPI().createLO();
        return new PostgresClob(pgConnection, oid);
    }

    @Override
    public Object convertToFieldType(IFieldMetaData field, Object value) {
        if (isBLobColumnName(field.getOriginalTypeName())) {
            return conversionHelper.convertValueToType(Blob.class, value, field.getFieldSubType());
        } else if (isCLobColumnName(field.getOriginalTypeName())) {
            return conversionHelper.convertValueToType(Clob.class, value, field.getFieldSubType());
        }
        return super.convertToFieldType(field, value);
    }

    @Override
    public Object convertFromFieldType(IDatabase database, IFieldMetaData field, Class<?> expectedType, Object value) {
        if (isBLobColumnName(field.getOriginalTypeName())) {
            var oid = conversionHelper.convertValueToType(Number.class, value).longValue();
            try {
                var connection = database.getAutowiredBeanInContext(Connection.class).unwrap(PGConnection.class);
                var blob = new PostgresBlob(connection, oid);
                Object targetValue = null;
                try {
                    targetValue = conversionHelper.convertValueToType(expectedType, blob);
                } finally {
                    if (targetValue != blob) {
                        blob.free();
                    }
                }
                return targetValue;
            } catch (SQLException e) {
                throw createPersistenceException(e, null);
            }
        } else if (isCLobColumnName(field.getOriginalTypeName())) {
            var oid = conversionHelper.convertValueToType(Number.class, value).longValue();
            try {
                var connection = database.getAutowiredBeanInContext(Connection.class).unwrap(PGConnection.class);
                var clob = new PostgresClob(connection, oid);

                Object targetValue = null;
                try {
                    targetValue = conversionHelper.convertValueToType(expectedType, clob);
                } finally {
                    if (targetValue != clob) {
                        clob.free();
                    }
                }
                return targetValue;
            } catch (SQLException e) {
                throw createPersistenceException(e, null);
            }
        }
        return super.convertFromFieldType(database, field, expectedType, value);
    }

    @SneakyThrows
    @Override
    protected ConnectionKeyValue preProcessConnectionIntern(Connection connection, String[] schemaNames, boolean forcePreProcessing) {
        try (var stm = connection.createStatement()) {
            stm.execute("SET SCHEMA '" + toDefaultCase(schemaNames[0]) + "'");
        }
        return scanForUndeferredDeferrableConstraints(connection, schemaNames);
    }

    @Override
    public void appendIsInOperatorClause(IAppendable appendable) {
        appendable.append(" = ANY");
    }

    @Override
    public void appendListClause(List<Object> parameters, IAppendable sb, Class<?> fieldType, IList<Object> splittedIds, Function<Object, Object> idDecompositor) {
        sb.append(" = ANY (?)");
        var connectionExtension = serviceContext.getService(IConnectionExtension.class);

        var javaArray = java.lang.reflect.Array.newInstance(fieldType, splittedIds.size());
        var index = 0;
        for (var id : splittedIds) {
            if (idDecompositor != null) {
                id = idDecompositor.apply(id);
            }
            var value = conversionHelper.convertValueToType(fieldType, id);
            java.lang.reflect.Array.set(javaArray, index, value);
            index++;
        }
        Array values = connectionExtension.createJDBCArray(fieldType, javaArray);

        ParamsUtil.addParam(parameters, values);
    }

    @Override
    protected String buildDeferrableForeignKeyConstraintsSelectSQL(String[] schemaNames) {
        var sb = new StringBuilder(
                "SELECT n.nspname AS OWNER, cl.relname AS TABLE_NAME, c.conname AS CONSTRAINT_NAME FROM pg_constraint c JOIN pg_namespace n ON c.connamespace=n.oid JOIN pg_class cl ON c" +
                        ".conrelid=cl" + ".oid WHERE c.condeferrable='t' AND c.condeferred='f' AND n.nspname");
        buildSchemaInClause(sb, schemaNames);
        return sb.toString();
    }

    @SneakyThrows
    @Override
    public IList<IMap<String, String>> getExportedKeys(Connection connection, String[] schemaNames) {
        var newSchemaNames = new String[schemaNames.length];
        System.arraycopy(schemaNames, 0, newSchemaNames, 0, schemaNames.length);
        for (int a = newSchemaNames.length; a-- > 0; ) {
            newSchemaNames[a] = newSchemaNames[a].toLowerCase();
        }
        var subselect = "SELECT ns.nspname AS \"owner\", con1.conname AS \"constraint_name\", unnest(con1.conkey) AS \"parent\", unnest(con1.confkey) AS \"child\", con1.confrelid, con1.conrelid"//
                + " FROM pg_class cl"//
                + " JOIN pg_namespace ns ON cl.relnamespace=ns.oid"//
                + " JOIN pg_constraint con1 ON con1.conrelid=cl.oid"//
                + " WHERE con1.contype='f' AND ns.nspname" + buildSchemaInClause(newSchemaNames);

        var sql = "select owner, constraint_name, cl2.relname as \"fk_table\", att2.attname as \"fk_column\", cl.relname as \"pk_table\", att.attname as \"pk_column\""//
                + " from (" + subselect + ") con"//
                + " JOIN pg_attribute att ON att.attrelid=con.confrelid AND att.attnum=con.child"//
                + " JOIN pg_class cl ON cl.oid=con.confrelid"//
                + " JOIN pg_class cl2 ON cl2.oid=con.conrelid"//
                + " JOIN pg_attribute att2 ON att2.attrelid = con.conrelid AND att2.attnum=con.parent";

        try (var pstm = connection.prepareStatement(sql); var allForeignKeysRS = pstm.executeQuery()) {
            var allForeignKeys = new ArrayList<IMap<String, String>>();
            while (allForeignKeysRS.next()) {
                var foreignKey = new HashMap<String, String>();

                foreignKey.put("OWNER", allForeignKeysRS.getString("owner"));
                foreignKey.put("CONSTRAINT_NAME", allForeignKeysRS.getString("constraint_name"));
                foreignKey.put("FKTABLE_NAME", allForeignKeysRS.getString("fk_table"));
                foreignKey.put("FKCOLUMN_NAME", allForeignKeysRS.getString("fk_column"));
                foreignKey.put("PKTABLE_NAME", allForeignKeysRS.getString("pk_table"));
                foreignKey.put("PKCOLUMN_NAME", allForeignKeysRS.getString("pk_column"));

                allForeignKeys.add(foreignKey);
            }
            return allForeignKeys;
        }
    }

    @Override
    public ILinkedMap<String, IList<String>> getFulltextIndexes(Connection connection, String schemaName) {
        LinkedHashMap<String, IList<String>> fulltextIndexes = new LinkedHashMap<>();
        // NOT YET IMPLEMENTED
        return fulltextIndexes;
    }

    @Override
    public boolean isSystemTable(String tableName) {
        return BIN_TABLE_NAME.matcher(tableName).matches() || IDX_TABLE_NAME.matcher(tableName).matches();
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint, Connection connection) {
    }

    @Override
    public int getResourceBusyErrorCode() {
        return 54;
    }

    @Override
    public PersistenceException createPersistenceException(SQLException e, String relatedSql) {
        String sqlState = e.getSQLState();

        SQLException sqlRootCause = e;
        while (sqlRootCause instanceof SQLException) {
            SQLException cause = sqlRootCause.getNextException();
            if (cause == null) {
                break;
            }
            sqlRootCause = cause;
        }
        if (SQLState.NULL_CONSTRAINT.getXopen().equals(sqlState)) {
            NullConstraintException ex = new NullConstraintException(sqlRootCause.getMessage(), relatedSql, e);
            ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
            return ex;
        } else if (SQLState.UNIQUE_CONSTRAINT.getXopen().equals(sqlState)) {
            UniqueConstraintException ex = new UniqueConstraintException(sqlRootCause.getMessage(), relatedSql, e);
            ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
            return ex;
        } else if (SQLState.LOCK_NOT_AVAILABLE.getXopen().equals(sqlState)) {
            PessimisticLockException ex = new PessimisticLockException(relatedSql, sqlRootCause);
            ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
            return ex;
        }
        int errorCode = e.getErrorCode();

        if (errorCode == getOptimisticLockErrorCode()) {
            OptimisticLockException ex = new OptimisticLockException(relatedSql, sqlRootCause);
            ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
            return ex;
        }
        if (errorCode == 1400) {
        }

        PersistenceException ex = new PersistenceException(relatedSql, e);
        ex.setStackTrace(e.getStackTrace());

        return ex;
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
        if (componentType == null) {
            return null;
        }
        String[] fieldTypeName = typeToArrayTypeNameMap.get(componentType);
        if (fieldTypeName == null) {
            throw new IllegalArgumentException("Can not handle component type '" + componentType + "'");
        }
        return fieldTypeName[0];
    }

    @Override
    public List<String> getAllFullqualifiedSequences(Connection connection, String... schemaNames) {
        List<String> allSequenceNames = new ArrayList<>();

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT t.sequence_schema || '.' || t.sequence_name FROM information_schema.sequences t WHERE t.sequence_schema" + buildSchemaInClause(schemaNames));
            while (rs.next()) {
                String fqSequenceName = rs.getString(1);
                allSequenceNames.add(fqSequenceName);
            }
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        } finally {
            JdbcUtil.close(stmt, rs);
        }

        return allSequenceNames;
    }

    @SneakyThrows
    @Override
    public List<String> getAllFullqualifiedTableNames(Connection connection, String... schemaNames) {
        List<String> allTableNames = new ArrayList<>();

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT t.table_schema || '.' || t.table_name FROM information_schema.tables t WHERE t.table_schema" + buildSchemaInClause(schemaNames));
            while (rs.next()) {
                String fqTableName = rs.getString(1);
                allTableNames.add(fqTableName);
            }
        } finally {
            JdbcUtil.close(stmt, rs);
        }

        return allTableNames;
    }

    @SneakyThrows
    @Override
    public List<String> getAllFullqualifiedViews(Connection connection, String... schemaNames) {
        List<String> allViewNames = new ArrayList<>();

        Statement stmt = null;
        ResultSet rs = null;
        try {
            for (String schemaName : schemaNames) {
                rs = connection.getMetaData().getTables(null, schemaName, null, new String[] { "VIEW" });

                while (rs.next()) {
                    // String schemaName = rs.getString("TABLE_SCHEM");
                    String viewName = rs.getString("TABLE_NAME");
                    if (!BIN_TABLE_NAME.matcher(viewName).matches() && !IDX_TABLE_NAME.matcher(viewName).matches()) {
                        allViewNames.add(schemaName + "." + viewName);
                    }
                }
            }
        } finally {
            JdbcUtil.close(stmt, rs);
        }

        return allViewNames;
    }

    @Override
    protected void handleRow(String schemaName, String tableName, String constraintName, com.koch.ambeth.util.collections.ArrayList<String> disableConstraintsSQL,
            com.koch.ambeth.util.collections.ArrayList<String> enableConstraintsSQL) {
        String fullName = "\"" + schemaName + "\".\"" + constraintName + "\"";
        disableConstraintsSQL.add("SET CONSTRAINTS " + fullName + " DEFERRED");
        enableConstraintsSQL.add("SET CONSTRAINTS " + fullName + " IMMEDIATE");
    }

    @Override
    public int getColumnCountForLinkTable() {
        return 3;
    }

    @SneakyThrows
    @Override
    public IList<IColumnEntry> getAllFieldsOfTable(Connection connection, String fqTableName) {
        String[] names = sqlBuilder.getSchemaAndTableName(fqTableName);
        ResultSet tableColumnsRS = connection.getMetaData().getColumns(null, names[0], names[1], null);
        try {
            ArrayList<IColumnEntry> columns = new ArrayList<>();
            columns.add(new ColumnEntry("ctid", -1, Object.class, null, false, 0, false));

            while (tableColumnsRS.next()) {
                String fieldName = tableColumnsRS.getString("COLUMN_NAME");
                int columnIndex = tableColumnsRS.getInt("ORDINAL_POSITION");
                int typeIndex = tableColumnsRS.getInt("DATA_TYPE");

                String typeName = tableColumnsRS.getString("TYPE_NAME");
                while (typeName.startsWith("_")) {
                    typeName = typeName.substring(1) + "[]";
                }
                String isNullable = tableColumnsRS.getString("IS_NULLABLE");
                boolean nullable = "YES".equalsIgnoreCase(isNullable);

                int scale = tableColumnsRS.getInt("COLUMN_SIZE");
                int digits = tableColumnsRS.getInt("DECIMAL_DIGITS");
                int radix = tableColumnsRS.getInt("NUM_PREC_RADIX");

                Class<?> javaType = JdbcUtil.getJavaTypeFromJdbcType(typeIndex, scale, digits);
                if ("lo".equalsIgnoreCase(typeName)) {
                    javaType = Blob.class;
                } else if ("text".equalsIgnoreCase(typeName)) {
                    javaType = String.class;
                }
                ColumnEntry entry = new ColumnEntry(fieldName, columnIndex, javaType, typeName, nullable, radix, true);
                columns.add(entry);
            }
            return columns;
        } finally {
            JdbcUtil.close(tableColumnsRS);
        }
    }

    @Override
    public boolean isTransactionNecessaryDuringLobStreaming() {
        return true;
    }

    @Override
    public String prepareCommand(String sqlCommand) {
        var matcher = pattern.matcher(sqlCommand);
        if (matcher.matches()) {
            var arrayTypeName = matcher.group(1);
            if ("STRING_ARRAY".equals(arrayTypeName) || "\"STRING_ARRAY\"".equals(arrayTypeName)) {
                return "";
            }
        }

        sqlCommand = prepareCommandIntern(sqlCommand, " BLOB", " LO");
        sqlCommand = prepareCommandIntern(sqlCommand, " CLOB", " TEXT");

        sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *1 *, *0 *\\)", " BOOLEAN");
        sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *[0-9] *, *0 *\\)", " INTEGER");
        sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *1[0,1,2,3,4,5,6,7,8] *, *0 *\\)", " BIGINT");
        sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *\\d+ *\\, *\\d+ *\\)", " NUMERIC");
        sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *\\* *\\, *\\d+ *\\)", " NUMERIC");
        sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *\\d+ *\\)", " NUMERIC");
        sqlCommand = prepareCommandInternWithGroup(sqlCommand, " NUMBER([^\"])", " NUMERIC\\2");
        // sqlCommand = prepareCommandIntern(sqlCommand, "(?: |\")NUMBER *\\(", " NUMERIC\\(");

        sqlCommand = prepareCommandInternWithGroup(sqlCommand, " VARCHAR *\\( *(\\d+) +CHAR *\\)", " VARCHAR(\\2)");

        sqlCommand = prepareCommandInternWithGroup(sqlCommand, " VARCHAR2 *\\( *(\\d+) +BYTE\\)", " VARCHAR(\\2)");
        sqlCommand = prepareCommandInternWithGroup(sqlCommand, " VARCHAR2 *\\( *(\\d+) +CHAR\\)", " VARCHAR(\\2)");

        sqlCommand = prepareCommandInternWithGroup(sqlCommand, " PRIMARY KEY (\\([^\\)]+\\)) USING INDEX", " PRIMARY KEY \\2");
        sqlCommand = prepareCommandInternWithGroup(sqlCommand, " PRIMARY KEY (\\([^\\)]+\\)) USING INDEX", " PRIMARY KEY \\2");

        sqlCommand = prepareCommandInternWithGroup(sqlCommand, "([^a-zA-Z0-9])STRING_ARRAY([^a-zA-Z0-9])", "\\2TEXT[]\\3");

        sqlCommand = prepareCommandIntern(sqlCommand, " NOORDER", "");
        sqlCommand = prepareCommandIntern(sqlCommand, " NOCYCLE", "");
        sqlCommand = prepareCommandIntern(sqlCommand, " USING +INDEX", "");

        sqlCommand = prepareCommandIntern(sqlCommand, " 999999999999999999999999999 ", " 9223372036854775807 ");

        sqlCommand = prepareCommandIntern(sqlCommand, "\\s+TABLESPACE\\s+[a-zA-Z0-9_]+", " ");

        sqlCommand = prepareCommandInternWithGroup(sqlCommand, "(\"?[A-Za-z0-9_]+\"?)\\.NEXTVAL", "nextval('\\2')");

        // Pattern tablespacePattern =
        // Pattern.compile("CREATE\\s+TABLESPACE\\s+([\\S]+)\\s*.*\\sDATAFILE\\s+'([^']+)'.*",
        // Pattern.CASE_INSENSITIVE);
        // Matcher tablespaceMatcher = tablespacePattern.matcher(sqlCommand);
        // if (tablespaceMatcher.matches())
        // {
        // String tablespace = tablespaceMatcher.group(1);
        // String file = tablespaceMatcher.group(2);
        //
        // sqlCommand = "CREATE TABLESPACE " + tablespace + " LOCATION '" + file + "'";
        // }
        return sqlCommand;
    }

    @Override
    public IOperand getLimitOperand(IOperand operand, IValueOperand valueOperand) {
        return beanContext.registerBean(LimitByLimitOperator.class)//
                          .propertyValue("Operand", operand)//
                          .propertyValue("ValueOperand", operand)//
                          .finish();

    }

    @Override
    public SelectPosition getLimitPosition() {
        return SelectPosition.AFTER_WHERE;
    }

    @Override
    public Class<?>[] getConnectionInterfaces(Connection connection) {
        if (connection instanceof BaseConnection) {
            return new Class<?>[] { BaseConnection.class, PGConnection.class };
        }
        return super.getConnectionInterfaces(connection);
    }
}
