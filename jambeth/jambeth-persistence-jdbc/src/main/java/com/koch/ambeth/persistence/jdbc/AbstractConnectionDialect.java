package com.koch.ambeth.persistence.jdbc;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.merge.metadata.MemberTypeProvider;
import com.koch.ambeth.persistence.ArrayQueryItem;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.connection.IConnectionKeyHandle;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;
import com.koch.ambeth.persistence.jdbc.sql.DefaultSqlRegexpLikeOperand;
import com.koch.ambeth.persistence.jdbc.sql.LimitByRownumOperator;
import com.koch.ambeth.persistence.sql.ParamsUtil;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IValueOperand;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.StateRollback;
import jakarta.transaction.TransactionManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.regex.Pattern;

public abstract class AbstractConnectionDialect implements IConnectionDialect, IInitializingBean, IDisposableBean {
    protected final WeakHashMap<IConnectionKeyHandle, ConnectionKeyValue> connectionToConstraintSqlMap = new WeakHashMap<>();
    protected final Lock writeLock = new ReentrantLock();
    protected Pattern dotPattern = Pattern.compile(".", Pattern.LITERAL);
    @Autowired
    protected IConversionHelper conversionHelper;
    @Autowired
    protected IDatabaseConnectionUrlProvider databaseConnectionUrlProvider;
    @Autowired
    protected IProperties props;
    @Autowired
    protected IThreadLocalObjectCollector objectCollector;
    @Autowired
    protected IServiceContext beanContext;
    @Autowired(optional = true)
    protected ITransactionState transactionState;
    @Property(name = PersistenceConfigurationConstants.ExternalTransactionManager, defaultValue = "false")
    protected boolean externalTransactionManager;
    @Property(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName)
    protected String schemaName;
    @Property(name = PersistenceJdbcConfigurationConstants.RegisterDriverEagerly, defaultValue = "true")
    protected boolean registerDriverEagerly;

    @Property(name = PersistenceConfigurationConstants.BatchSize, defaultValue = "1000")
    protected int batchSizeDefault;

    @Property(name = PersistenceConfigurationConstants.PreparedBatchSize, defaultValue = "1000")
    protected int preparedBatchSizeDefault;

    @Property(name = PersistenceConfigurationConstants.BatchSize, mandatory = false)
    protected int batchSize;

    @Property(name = PersistenceConfigurationConstants.PreparedBatchSize, mandatory = false)
    protected int preparedBatchSize;

    @Autowired(optional = true)
    protected TransactionManager transactionManager;
    protected String[] schemaNames;
    protected Driver driverRegisteredExplicitly;
    protected boolean doDirectClobConversion = true;

    @Override
    public void afterPropertiesSet() throws Throwable {
        registerDriverIfNeeded();

        schemaNames = toDefaultCase(schemaName).split("[:;]");
    }

    protected abstract Class<?> getDriverType();

    @Override
    public void appendIsInOperatorClause(IAppendable appendable) {
        appendable.append(" IN ");
    }

    @Override
    public void appendListClause(List<Object> parameters, IAppendable sb, Class<?> fieldType, List<Object> splittedIds, Function<Object, Object> idDecompositor) {
        sb.append(" IN (");

        var preparedConverter = conversionHelper.prepareConverter(fieldType);
        for (int b = 0, sizeB = splittedIds.size(); b < sizeB; b++) {
            var id = idDecompositor.apply(splittedIds.get(b));
            var value = preparedConverter.convertValue(id, null);
            if (b > 0) {
                sb.append(',');
            }
            sb.append('?');
            ParamsUtil.addParam(parameters, value);
        }

        sb.append(')');
    }

    @Override
    public boolean isCompactMultiValueRecommended(List<Object> values) {
        return values.size() > getMaxInClauseBatchThreshold();
    }

    @Override
    public IOperand getRegexpLikeFunction(IOperand sourceString, IOperand pattern, IOperand matchParameter) {
        return beanContext.registerBean(DefaultSqlRegexpLikeOperand.class)
                          .propertyValue("SourceString", sourceString)
                          .propertyValue("Pattern", pattern)
                          .propertyValue("MatchParameter", matchParameter)
                          .finish();
    }

    @Override
    public IOperand getLimitOperand(IOperand operand, IValueOperand valueOperand) {
        return beanContext.registerBean(LimitByRownumOperator.class)//
                          .propertyValue("Operand", operand)//
                          .propertyValue("ValueOperand", operand)//
                          .finish();
    }

    @SneakyThrows
    @Override
    public Blob createBlob(Connection connection) {
        return connection.createBlob();
    }

    @SneakyThrows
    @Override
    public void releaseBlob(Blob blob) {
        blob.free();
    }

    @SneakyThrows
    @Override
    public Clob createClob(Connection connection) {
        return connection.createClob();
    }

    @SneakyThrows
    @Override
    public void releaseClob(Clob clob) {
        clob.free();
    }

    @Override
    @SneakyThrows
    public void releaseArray(Array array) {
        array.free();
    }

    @Override
    public Object convertToFieldType(IFieldMetaData field, Object value) {
        return conversionHelper.convertValueToType(field.getFieldType(), value, field.getFieldSubType());
    }

    @Override
    public Object convertFromFieldType(IDatabase database, IFieldMetaData field, Class<?> expectedType, Object value) {
        if (value instanceof Clob) {
            if (doDirectClobConversion) {
                try {
                    return conversionHelper.convertValueToType(expectedType, value);
                } catch (IllegalArgumentException e) {
                    // try only once
                    doDirectClobConversion = false;
                }
            }
            String sValue = conversionHelper.convertValueToType(String.class, value);
            return conversionHelper.convertValueToType(expectedType, sValue);
        }
        return conversionHelper.convertValueToType(expectedType, value);
    }

    @Override
    public String toDefaultCase(String identifier) {
        return identifier.toUpperCase(); // uppercase is the SQL standard
    }

    @SneakyThrows
    protected void registerDriverIfNeeded() {
        if (!registerDriverEagerly) {
            return;
        }
        Class<?> databaseDriver = getDriverType();
        if (databaseDriver == null || !Driver.class.isAssignableFrom(databaseDriver)) {
            return;
        }
        try {
            DriverManager.getDriver(databaseConnectionUrlProvider.getConnectionUrl());
        } catch (SQLException e) {
            if (!"08001".equals(e.getSQLState())) {
                throw e;
            }
            driverRegisteredExplicitly = (Driver) databaseDriver.newInstance();
            DriverManager.registerDriver(driverRegisteredExplicitly);
        }
    }

    @Override
    public void destroy() throws Throwable {
        if (driverRegisteredExplicitly != null) {
            DriverManager.deregisterDriver(driverRegisteredExplicitly);
            driverRegisteredExplicitly = null;
        }
    }

    @Override
    public final int getBatchSize() {
        if (batchSize > 0) {
            return batchSize;
        }
        return getDefaultBatchSizeOfDialect();
    }

    protected int getDefaultBatchSizeOfDialect() {
        return batchSizeDefault;
    }

    @Override
    public final int getPreparedBatchSize() {
        if (preparedBatchSize > 0) {
            return preparedBatchSize;
        }
        return getDefaultPreparedBatchSizeOfDialect();
    }

    protected int getDefaultPreparedBatchSizeOfDialect() {
        return preparedBatchSizeDefault;
    }

    @Override
    public int getMaxInClauseBatchThreshold() {
        return 4000;
    }

    @Override
    public void handleWithMultiValueLeftField(IAppendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters, List<List<Object>> splitValues, boolean caseSensitive,
            Class<?> leftOperandFieldType) {
        querySB.append("SELECT COLUMN_VALUE FROM (");
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
            querySB.append(",ROWNUM FROM TABLE(?)");
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
                    querySB.append(",ROWNUM");
                }
                querySB.append(" FROM TABLE(?)");
                if (size > 1) {
                    querySB.append(')');
                }
            }
        }
        querySB.append(')');
    }

    @SneakyThrows
    @Override
    public void preProcessConnection(Connection connection, String[] schemaNames, boolean forcePreProcessing) {
        ConnectionKeyValue connectionKeyValue = null;
        IConnectionKeyHandle connectionKeyHandle = null;
        var writeLock = this.writeLock;

        if (connection.isWrapperFor(IConnectionKeyHandle.class)) {
            connectionKeyHandle = connection.unwrap(IConnectionKeyHandle.class);
            writeLock.lock();
            try {
                // WeakHashMaps have ALWAYS to be exclusively locked even if they SEEM to be only
                // read-accessed
                connectionKeyValue = connectionToConstraintSqlMap.get(connectionKeyHandle);
            } finally {
                writeLock.unlock();
            }
        }
        if (forcePreProcessing || connectionKeyValue == null) {
            if (connectionKeyHandle == null) {
                throw new IllegalStateException("Should never happen");
            }
            if (schemaNames == null && connectionKeyValue != null) {
                schemaNames = connectionKeyValue.getSchemaNames();
            }
            connectionKeyValue = preProcessConnectionIntern(connection, schemaNames, forcePreProcessing);
            writeLock.lock();
            try {
                connectionToConstraintSqlMap.put(connectionKeyHandle, connectionKeyValue);
            } finally {
                writeLock.unlock();
            }
        }
    }

    protected ConnectionKeyValue preProcessConnectionIntern(Connection connection, String[] schemaNames, boolean forcePreProcessing) {
        return new ConnectionKeyValue(new String[0], new String[0], new String[0]);
    }

    @SneakyThrows
    @Override
    public IStateRollback disableConstraints(final Connection connection, String... schemaNames) {
        var connectionKeyValue = resolveConnectionKeyValue(connection);
        var constraintSql = connectionKeyValue.getDisableConstraintsSQL();

        if (constraintSql.length == 0) {
            return StateRollback.empty();
        }
        try (var stm = connection.createStatement()) {
            for (int a = 0, size = constraintSql.length; a < size; a++) {
                stm.addBatch(constraintSql[a]);
            }
            stm.executeBatch();
        }
        return () -> {
            var enableConstraintsSQL = connectionKeyValue.getEnableConstraintsSQL();
            if (enableConstraintsSQL.length == 0) {
                return;
            }
            try (var stmt = connection.createStatement()) {
                for (int i = enableConstraintsSQL.length; i-- > 0; ) {
                    stmt.addBatch(enableConstraintsSQL[i]);
                }
                stmt.executeBatch();
            } catch (Exception e) {
                throw RuntimeExceptionUtil.mask(e);
            }
        };
    }

    @SneakyThrows
    protected ConnectionKeyValue resolveConnectionKeyValue(Connection connection) {
        if (!connection.isWrapperFor(IConnectionKeyHandle.class)) {
            throw new IllegalStateException("Connection is not a wrapper for " + IConnectionKeyHandle.class.getName());
        }
        var connectionKeyHandle = connection.unwrap(IConnectionKeyHandle.class);
        var writeLock = this.writeLock;
        writeLock.lock();
        try {
            // WeakHashMaps have ALWAYS to be exclusively locked even if they SEEM to be only
            // read-accessed
            return connectionToConstraintSqlMap.get(connectionKeyHandle);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean useVersionOnOptimisticUpdate() {
        return false;
    }

    @SneakyThrows
    @Override
    public void commit(Connection connection) {
        var active = transactionState != null ? transactionState.isExternalTransactionManagerActive() : null;
        if (active == null) {
            active = Boolean.valueOf(externalTransactionManager);
        }
        if (active.booleanValue()) {
            // No Action!
            // Transactions are externally managed.
        } else {
            connection.commit();
        }
    }

    @SneakyThrows
    @Override
    public void rollback(Connection connection) {
        var active = transactionState != null ? transactionState.isExternalTransactionManagerActive() : null;
        if (active == null) {
            active = Boolean.valueOf(externalTransactionManager);
        }
        if (active.booleanValue()) {
            // If transaction is externally managed and a rollback is required, tell the transaction
            // manager
            if (transactionManager != null) {
                var transaction = transactionManager.getTransaction();
                if (transaction != null) {
                    transaction.setRollbackOnly();
                }
            }
        } else if (connection != null) {
            connection.rollback();
        }
    }

    @SneakyThrows
    protected void printResultSet(ResultSet rs) {
        var metaData = rs.getMetaData();
        var columnCount = metaData.getColumnCount();
        for (int a = 0, size = columnCount; a < size; a++) {
            System.out.print(metaData.getColumnLabel(a + 1));
            System.out.print("\t\t");
        }
        System.out.println("\t\t");
        while (rs.next()) {
            for (int a = 0, size = columnCount; a < size; a++) {
                System.out.print(rs.getObject(a + 1));
                System.out.print("\t\t");
            }
            System.out.println();
        }
    }

    @SneakyThrows
    protected ConnectionKeyValue scanForUndeferredDeferrableConstraints(Connection connection, String[] schemaNames) {
        var disableConstraintsSQL = new ArrayList<String>();
        var enableConstraintsSQL = new ArrayList<String>();
        var sql = buildDeferrableForeignKeyConstraintsSelectSQL(schemaNames);
        try (var stm = connection.createStatement(); var rs = stm.executeQuery(sql)) {
            if (sql != null) {
                while (rs.next()) {
                    var schemaName = rs.getString("OWNER");
                    var tableName = rs.getString("TABLE_NAME");
                    var constraintName = rs.getString("CONSTRAINT_NAME");

                    handleRow(schemaName, tableName, constraintName, disableConstraintsSQL, enableConstraintsSQL);
                }
            }
            var disableConstraintsArray = disableConstraintsSQL.toArray(String[]::new);
            var enabledConstraintsArray = enableConstraintsSQL.toArray(String[]::new);
            return new ConnectionKeyValue(schemaNames, disableConstraintsArray, enabledConstraintsArray);
        }
    }

    protected abstract String buildDeferrableForeignKeyConstraintsSelectSQL(String[] schemaNames);

    protected abstract void handleRow(String schemaName, String tableName, String constraintName, ArrayList<String> disableConstraintsSQL, ArrayList<String> enableConstraintsSQL);

    protected String buildSchemaInClause(final String... schemaNames) {
        var sb = new StringBuilder();
        buildSchemaInClause(sb, schemaNames);
        return sb.toString();
    }

    protected void buildSchemaInClause(final StringBuilder sb, final String... schemaNames) {
        sb.append(" IN (");
        var first = true;
        for (int a = schemaNames.length; a-- > 0; ) {
            if (!first) {
                sb.append(',');
            }
            sb.append('\'').append(schemaNames[a]).append('\'');
            first = false;
        }
        sb.append(')');
    }

    @Override
    public boolean isTransactionNecessaryDuringLobStreaming() {
        return false;
    }

    @Override
    public boolean isEmptyStringAsNullStored(IFieldMetaData field) {
        return false;
    }

    protected String prepareCommandIntern(String sqlCommand, String regex, String replacement) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(sqlCommand).replaceAll(replacement);
    }

    protected String prepareCommandInternWithGroup(String sqlCommand, String regex, String replacement) {
        var pattern = Pattern.compile("(.*?)" + regex + "(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        return concat(sqlCommand, replacement, pattern);
    }

    protected String concat(String sqlCommand, String replacement, Pattern pattern) {
        var matcher = pattern.matcher(sqlCommand);
        if (!matcher.matches()) {
            return sqlCommand;
        }
        var left = concat(matcher.group(1), replacement, pattern);
        var right = concat(matcher.group(matcher.groupCount()), replacement, pattern);
        for (int a = 2; a < matcher.groupCount(); a++) {
            replacement = replacement.replace("\\" + a, matcher.group(a));
        }
        return left + replacement + right;
    }

    @SneakyThrows
    @Override
    public List<String> queryDefault(Connection connection, String resultColumnName, String sql, Object... args) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            if (args.length > 0) {
                var pstm = connection.prepareStatement(sql);
                stmt = pstm;
                for (int a = args.length; a-- > 0; ) {
                    pstm.setObject(a + 1, args[0]);
                }
                rs = pstm.executeQuery();
            } else {
                stmt = connection.createStatement();
                rs = stmt.executeQuery(sql);
            }
            var result = new ArrayList<String>();
            while (rs.next()) {
                result.add(rs.getString(resultColumnName));
            }
            return result;
        } finally {
            JdbcUtil.close(stmt, rs);
        }
    }

    @Override
    public int getColumnCountForLinkTable() {
        return 2;
    }

    @Override
    public String escapeName(CharSequence symbolName) {
        var escapeLiteral = getEscapeLiteral();
        if (symbolName.length() == 0) {
            // already escaped
            return symbolName.toString();
        }
        if (escapeLiteral.length() <= symbolName.length()) {
            var alreadyEscaped = true;
            for (int a = escapeLiteral.length(); a-- > 0; ) {
                if (symbolName.charAt(a) != escapeLiteral.charAt(a)) {
                    alreadyEscaped = false;
                    break;
                }
            }
            if (alreadyEscaped) {
                return symbolName.toString();
            }
        }
        for (int a = symbolName.length(); a-- > 0; ) {
            if (symbolName.charAt(a) == '.') {
                var dotReplacedName = dotPattern.matcher(symbolName).replaceAll(escapeLiteral + '.' + escapeLiteral);
                return StringBuilderUtil.concat(objectCollector.getCurrent(), escapeLiteral, dotReplacedName, escapeLiteral);
            }
        }
        // no dots in the symbolName. this saves us the Regex operation
        return StringBuilderUtil.concat(objectCollector.getCurrent(), escapeLiteral, symbolName, escapeLiteral);
    }

    @Override
    public IAppendable escapeName(CharSequence symbolName, IAppendable sb) {
        var escapeLiteral = getEscapeLiteral();
        if (symbolName.length() == 0) {
            // already escaped
            return sb.append(symbolName);
        }
        if (escapeLiteral.length() <= symbolName.length()) {
            boolean alreadyEscaped = true;
            for (int a = escapeLiteral.length(); a-- > 0; ) {
                if (symbolName.charAt(a) != escapeLiteral.charAt(a)) {
                    alreadyEscaped = false;
                    break;
                }
            }
            if (alreadyEscaped) {
                return sb.append(symbolName);
            }
        }
        for (int a = symbolName.length(); a-- > 0; ) {
            if (symbolName.charAt(a) == '.') {
                var dotReplacedName = dotPattern.matcher(symbolName).replaceAll(escapeLiteral + '.' + escapeLiteral);
                return sb.append(escapeLiteral).append(dotReplacedName).append(escapeLiteral);
            }
        }
        // no dots in the symbolName. this saves us the Regex operation
        return sb.append(escapeLiteral).append(symbolName).append(escapeLiteral);
    }

    @Override
    public String escapeSchemaAndSymbolName(CharSequence schemaName, CharSequence symbolName) {
        var escapeLiteral = getEscapeLiteral();
        if (schemaName == null) {
            return StringBuilderUtil.concat(objectCollector.getCurrent(), escapeLiteral, symbolName, escapeLiteral);
        }
        return StringBuilderUtil.concat(objectCollector.getCurrent(), escapeLiteral, schemaName, escapeLiteral, ".", escapeLiteral, symbolName, escapeLiteral);
    }

    @Override
    public String getEscapeLiteral() {
        return "\"";
    }

    @Override
    public String buildClearTableSQL(String tableName) {
        return "DELETE FROM " + escapeName(tableName) + " CASCADE";
    }

    @Override
    public String getSelectForUpdateFragment() {
        return " FOR UPDATE NOWAIT";
    }

    @Override
    public Class<?>[] getConnectionInterfaces(Connection connection) {
        return MemberTypeProvider.EMPTY_TYPES;
    }

    @RequiredArgsConstructor
    @Getter
    public static class ConnectionKeyValue {
        final String[] schemaNames;

        final String[] disableConstraintsSQL;

        final String[] enableConstraintsSQL;
    }
}
