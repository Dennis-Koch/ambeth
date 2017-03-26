package com.koch.ambeth.persistence.jdbc;

import java.sql.Array;

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

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ITransactionState;
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
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.state.IStateRollback;

public abstract class AbstractConnectionDialect
		implements IConnectionDialect, IInitializingBean, IDisposableBean {
	public static class ConnectionKeyValue {
		protected String[] disableConstraintsSQL;

		protected String[] enableConstraintsSQL;

		public ConnectionKeyValue(String[] disableConstraintsSQL, String[] enableConstraintsSQL) {
			super();
			this.disableConstraintsSQL = disableConstraintsSQL;
			this.enableConstraintsSQL = enableConstraintsSQL;
		}

		public String[] getDisableConstraintsSQL() {
			return disableConstraintsSQL;
		}

		public String[] getEnableConstraintsSQL() {
			return enableConstraintsSQL;
		}
	}

	protected Pattern dotPattern = Pattern.compile(".", Pattern.LITERAL);

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

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

	@Property(name = PersistenceConfigurationConstants.ExternalTransactionManager,
			defaultValue = "false")
	protected boolean externalTransactionManager;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName)
	protected String schemaName;

	@Property(name = PersistenceJdbcConfigurationConstants.RegisterDriverEagerly,
			defaultValue = "true")
	protected boolean registerDriverEagerly;

	@Autowired(optional = true)
	protected TransactionManager transactionManager;

	protected String[] schemaNames;

	protected Driver driverRegisteredExplicitly;

	protected final WeakHashMap<IConnectionKeyHandle, ConnectionKeyValue> connectionToConstraintSqlMap =
			new WeakHashMap<>();

	protected final Lock writeLock = new ReentrantLock();

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
	public void appendListClause(List<Object> parameters, IAppendable sb, Class<?> fieldType,
			IList<Object> splittedIds) {
		sb.append(" IN (");

		for (int b = 0, sizeB = splittedIds.size(); b < sizeB; b++) {
			Object id = splittedIds.get(b);
			if (b > 0) {
				sb.append(',');
			}
			sb.append('?');
			ParamsUtil.addParam(parameters, id);
		}

		sb.append(')');
	}

	@Override
	public boolean isCompactMultiValueRecommended(IList<Object> values) {
		return values.size() > getMaxInClauseBatchThreshold();
	}

	@Override
	public IOperand getRegexpLikeFunction(IOperand sourceString, IOperand pattern,
			IOperand matchParameter) {
		return beanContext.registerBean(DefaultSqlRegexpLikeOperand.class)
				.propertyValue("SourceString", sourceString).propertyValue("Pattern", pattern)
				.propertyValue("MatchParameter", matchParameter).finish();
	}

	@Override
	public IOperand getLimitOperand(IOperand operand, IValueOperand valueOperand) {
		return beanContext.registerBean(LimitByRownumOperator.class)//
				.propertyValue("Operand", operand)//
				.propertyValue("ValueOperand", operand)//
				.finish();
	}

	@Override
	public Blob createBlob(Connection connection) throws SQLException {
		return connection.createBlob();
	}

	@Override
	public void releaseBlob(Blob blob) throws SQLException {
		blob.free();
	}

	@Override
	public Clob createClob(Connection connection) throws SQLException {
		return connection.createClob();
	}

	@Override
	public void releaseClob(Clob clob) throws SQLException {
		clob.free();
	}

	@Override
	public void releaseArray(Array array) throws SQLException {
		array.free();
	}

	@Override
	public Object convertToFieldType(IFieldMetaData field, Object value) {
		return conversionHelper.convertValueToType(field.getFieldType(), value,
				field.getFieldSubType());
	}

	@Override
	public Object convertFromFieldType(IDatabase database, IFieldMetaData field,
			Class<?> expectedType, Object value) {
		if (value instanceof Clob) {
			if (doDirectClobConversion) {
				try {
					return conversionHelper.convertValueToType(expectedType, value);
				}
				catch (IllegalArgumentException e) {
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

	protected void registerDriverIfNeeded() {
		if (!registerDriverEagerly) {
			return;
		}
		Class<?> databaseDriver = getDriverType();
		if (databaseDriver == null || !Driver.class.isAssignableFrom(databaseDriver)) {
			return;
		}
		try {
			try {
				DriverManager.getDriver(databaseConnectionUrlProvider.getConnectionUrl());
			}
			catch (SQLException e) {
				if (!"08001".equals(e.getSQLState())) {
					throw e;
				}
				driverRegisteredExplicitly = (Driver) databaseDriver.newInstance();
				DriverManager.registerDriver(driverRegisteredExplicitly);
			}
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
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
	public int getMaxInClauseBatchThreshold() {
		return 4000;
	}

	@Override
	public void handleWithMultiValueLeftField(IAppendable querySB,
			IMap<Object, Object> nameToValueMap, IList<Object> parameters,
			IList<IList<Object>> splitValues, boolean caseSensitive, Class<?> leftOperandFieldType) {
		querySB.append("SELECT COLUMN_VALUE FROM (");
		if (splitValues.size() == 0) {
			// Special scenario with EMPTY argument
			ArrayQueryItem aqi = new ArrayQueryItem(new Object[0], leftOperandFieldType);
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
		}
		else {
			String placeholder;
			if (caseSensitive) {
				placeholder = "COLUMN_VALUE";
			}
			else {
				placeholder = "LOWER(COLUMN_VALUE) COLUMN_VALUE";
			}

			for (int a = 0, size = splitValues.size(); a < size; a++) {
				IList<Object> values = splitValues.get(a);
				if (a > 0) {
					// A union allows us to suppress the "ROWNUM" column because table(?) will already get
					// materialized without it
					querySB.append(" UNION ");
				}
				if (size > 1) {
					querySB.append('(');
				}
				ArrayQueryItem aqi = new ArrayQueryItem(values.toArray(), leftOperandFieldType);
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

	@Override
	public void preProcessConnection(Connection connection, String[] schemaNames,
			boolean forcePreProcessing) {
		try {
			ConnectionKeyValue connectionKeyValue = null;
			IConnectionKeyHandle connectionKeyHandle = null;
			Lock writeLock = this.writeLock;

			if (connection.isWrapperFor(IConnectionKeyHandle.class)) {
				connectionKeyHandle = connection.unwrap(IConnectionKeyHandle.class);
				writeLock.lock();
				try {
					// WeakHashMaps have ALWAYS to be exclusively locked even if they SEEM to be only
					// read-accessed
					connectionKeyValue = connectionToConstraintSqlMap.get(connectionKeyHandle);
				}
				finally {
					writeLock.unlock();
				}
			}
			if (forcePreProcessing || connectionKeyValue == null) {
				if (connectionKeyHandle == null) {
					throw new IllegalStateException("Should never happen");
				}
				connectionKeyValue =
						preProcessConnectionIntern(connection, schemaNames, forcePreProcessing);
				writeLock.lock();
				try {
					connectionToConstraintSqlMap.put(connectionKeyHandle, connectionKeyValue);
				}
				finally {
					writeLock.unlock();
				}
			}
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected ConnectionKeyValue preProcessConnectionIntern(Connection connection,
			String[] schemaNames, boolean forcePreProcessing) throws SQLException {
		return new ConnectionKeyValue(new String[0], new String[0]);
	}

	@Override
	public IStateRollback disableConstraints(final Connection connection, String... schemaNames) {
		try {
			final ConnectionKeyValue connectionKeyValue;
			IConnectionKeyHandle connectionKeyHandle = null;

			if (connection.isWrapperFor(IConnectionKeyHandle.class)) {
				connectionKeyHandle = connection.unwrap(IConnectionKeyHandle.class);
				Lock writeLock = this.writeLock;
				writeLock.lock();
				try {
					// WeakHashMaps have ALWAYS to be exclusively locked even if they SEEM to be only
					// read-accessed
					connectionKeyValue = connectionToConstraintSqlMap.get(connectionKeyHandle);
				}
				finally {
					writeLock.unlock();
				}
			}
			else {
				throw new IllegalStateException(
						"Connection is not a wrapper for " + IConnectionKeyHandle.class.getName());
			}
			String[] constraintSql = connectionKeyValue.getDisableConstraintsSQL();

			if (constraintSql.length > 0) {
				Statement stm = connection.createStatement();
				try {
					for (int a = 0, size = constraintSql.length; a < size; a++) {
						stm.addBatch(constraintSql[a]);
					}
					stm.executeBatch();
				}
				finally {
					JdbcUtil.close(stm);
				}
			}
			return new IStateRollback() {
				@Override
				public void rollback() {
					String[] enableConstraintsSQL = connectionKeyValue.getEnableConstraintsSQL();
					Statement stmt = null;
					try {
						stmt = connection.createStatement();
						for (int i = enableConstraintsSQL.length; i-- > 0;) {
							stmt.addBatch(enableConstraintsSQL[i]);
						}
						stmt.executeBatch();
					}
					catch (Throwable e) {
						throw RuntimeExceptionUtil.mask(e);
					}
					finally {
						JdbcUtil.close(stmt);
					}
				}
			};
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public boolean useVersionOnOptimisticUpdate() {
		return false;
	}

	@Override
	public void commit(Connection connection) throws SQLException {
		Boolean active =
				transactionState != null ? transactionState.isExternalTransactionManagerActive() : null;
		if (active == null) {
			active = Boolean.valueOf(externalTransactionManager);
		}
		if (active.booleanValue()) {
			// No Action!
			// Transactions are externally managed.
		}
		else {
			connection.commit();
		}
	}

	@Override
	public void rollback(Connection connection) throws SQLException {
		Boolean active =
				transactionState != null ? transactionState.isExternalTransactionManagerActive() : null;
		if (active == null) {
			active = Boolean.valueOf(externalTransactionManager);
		}
		if (active.booleanValue()) {
			// If transaction is externally managed and a rollback is required, tell the transaction
			// manager
			if (transactionManager != null) {
				try {
					Transaction transaction = transactionManager.getTransaction();
					if (transaction != null) {
						transaction.setRollbackOnly();
					}
				}
				catch (SystemException e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
		else {
			connection.rollback();
		}
	}

	protected void printResultSet(ResultSet rs) throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
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

	protected ConnectionKeyValue scanForUndeferredDeferrableConstraints(Connection connection,
			String[] schemaNames) throws SQLException {
		Statement stm = connection.createStatement();
		try {
			ArrayList<String> disableConstraintsSQL = new ArrayList<>();
			ArrayList<String> enableConstraintsSQL = new ArrayList<>();
			String sql = buildDeferrableForeignKeyConstraintsSelectSQL(schemaNames);
			if (sql != null) {
				ResultSet rs = stm.executeQuery(sql);
				while (rs.next()) {
					String schemaName = rs.getString("OWNER");
					String tableName = rs.getString("TABLE_NAME");
					String constraintName = rs.getString("CONSTRAINT_NAME");

					handleRow(schemaName, tableName, constraintName, disableConstraintsSQL,
							enableConstraintsSQL);
				}
			}
			String[] disableConstraintsArray =
					disableConstraintsSQL.toArray(new String[disableConstraintsSQL.size()]);
			String[] enabledConstraintsArray =
					enableConstraintsSQL.toArray(new String[enableConstraintsSQL.size()]);
			ConnectionKeyValue connectionKeyValue =
					new ConnectionKeyValue(disableConstraintsArray, enabledConstraintsArray);

			return connectionKeyValue;
		}
		finally {
			JdbcUtil.close(stm);
		}
	}

	protected abstract String buildDeferrableForeignKeyConstraintsSelectSQL(String[] schemaNames);

	protected abstract void handleRow(String schemaName, String tableName, String constraintName,
			ArrayList<String> disableConstraintsSQL, ArrayList<String> enableConstraintsSQL);

	protected String buildSchemaInClause(final String... schemaNames) {
		StringBuilder sb = new StringBuilder();
		buildSchemaInClause(sb, schemaNames);
		return sb.toString();
	}

	protected void buildSchemaInClause(final StringBuilder sb, final String... schemaNames) {
		sb.append(" IN (");
		boolean first = true;
		for (int a = schemaNames.length; a-- > 0;) {
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
		return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(sqlCommand)
				.replaceAll(replacement);
	}

	protected String prepareCommandInternWithGroup(String sqlCommand, String regex,
			String replacement) {
		Pattern pattern =
				Pattern.compile("(.*?)" + regex + "(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		return concat(sqlCommand, replacement, pattern);
	}

	protected String concat(String sqlCommand, String replacement, Pattern pattern) {
		Matcher matcher = pattern.matcher(sqlCommand);
		if (!matcher.matches()) {
			return sqlCommand;
		}
		String left = concat(matcher.group(1), replacement, pattern);
		String right = concat(matcher.group(matcher.groupCount()), replacement, pattern);
		for (int a = 2; a < matcher.groupCount(); a++) {
			replacement = replacement.replace("\\" + a, matcher.group(a));
		}
		return left + replacement + right;
	}

	@Override
	public IList<String> queryDefault(Connection connection, String resultColumnName, String sql,
			Object... args) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			if (args.length > 0) {
				PreparedStatement pstm = connection.prepareStatement(sql);
				for (int a = args.length; a-- > 0;) {
					pstm.setObject(a + 1, args[0]);
				}
				rs = pstm.executeQuery();
				stmt = pstm;
			}
			else {
				stmt = connection.createStatement();
				rs = stmt.executeQuery(sql);
			}
			ArrayList<String> result = new ArrayList<>();
			while (rs.next()) {
				result.add(rs.getString(resultColumnName));
			}
			return result;
		}
		finally {
			JdbcUtil.close(stmt, rs);
		}
	}

	@Override
	public int getColumnCountForLinkTable() {
		return 2;
	}

	@Override
	public String escapeName(CharSequence symbolName) {
		String escapeLiteral = getEscapeLiteral();
		if (symbolName.length() == 0) {
			// already escaped
			return symbolName.toString();
		}
		if (escapeLiteral.length() <= symbolName.length()) {
			boolean alreadyEscaped = true;
			for (int a = escapeLiteral.length(); a-- > 0;) {
				if (symbolName.charAt(a) != escapeLiteral.charAt(a)) {
					alreadyEscaped = false;
					break;
				}
			}
			if (alreadyEscaped) {
				return symbolName.toString();
			}
		}
		for (int a = symbolName.length(); a-- > 0;) {
			if (symbolName.charAt(a) == '.') {
				String dotReplacedName =
						dotPattern.matcher(symbolName).replaceAll(escapeLiteral + '.' + escapeLiteral);
				return StringBuilderUtil.concat(objectCollector.getCurrent(), escapeLiteral,
						dotReplacedName, escapeLiteral);
			}
		}
		// no dots in the symbolName. this saves us the Regex operation
		return StringBuilderUtil.concat(objectCollector.getCurrent(), escapeLiteral, symbolName,
				escapeLiteral);
	}

	@Override
	public IAppendable escapeName(CharSequence symbolName, IAppendable sb) {
		String escapeLiteral = getEscapeLiteral();
		if (symbolName.length() == 0) {
			// already escaped
			return sb.append(symbolName);
		}
		if (escapeLiteral.length() <= symbolName.length()) {
			boolean alreadyEscaped = true;
			for (int a = escapeLiteral.length(); a-- > 0;) {
				if (symbolName.charAt(a) != escapeLiteral.charAt(a)) {
					alreadyEscaped = false;
					break;
				}
			}
			if (alreadyEscaped) {
				return sb.append(symbolName);
			}
		}
		for (int a = symbolName.length(); a-- > 0;) {
			if (symbolName.charAt(a) == '.') {
				String dotReplacedName =
						dotPattern.matcher(symbolName).replaceAll(escapeLiteral + '.' + escapeLiteral);
				return sb.append(escapeLiteral).append(dotReplacedName).append(escapeLiteral);
			}
		}
		// no dots in the symbolName. this saves us the Regex operation
		return sb.append(escapeLiteral).append(symbolName).append(escapeLiteral);
	}

	@Override
	public String escapeSchemaAndSymbolName(CharSequence schemaName, CharSequence symbolName) {
		String escapeLiteral = getEscapeLiteral();
		if (schemaName == null) {
			return StringBuilderUtil.concat(objectCollector.getCurrent(), escapeLiteral, symbolName,
					escapeLiteral);
		}
		return StringBuilderUtil.concat(objectCollector.getCurrent(), escapeLiteral, schemaName,
				escapeLiteral, ".", escapeLiteral, symbolName, escapeLiteral);
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
}
