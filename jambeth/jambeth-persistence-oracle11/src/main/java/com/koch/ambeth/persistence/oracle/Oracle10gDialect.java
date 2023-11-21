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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.persistence.PessimisticLockException;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.persistence.IColumnEntry;
import com.koch.ambeth.persistence.SQLState;
import com.koch.ambeth.persistence.SelectPosition;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.jdbc.AbstractConnectionDialect;
import com.koch.ambeth.persistence.jdbc.ColumnEntry;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.exception.NullConstraintException;
import com.koch.ambeth.persistence.jdbc.exception.UniqueConstraintException;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

import lombok.SneakyThrows;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.driver.OracleDriver;

public class Oracle10gDialect extends AbstractConnectionDialect {

	public static final Pattern BIN_TABLE_NAME = Pattern.compile("BIN\\$.{22}==\\$0",
			Pattern.CASE_INSENSITIVE);

	public static final Pattern IDX_TABLE_NAME = Pattern.compile("DR\\$.*?\\$.",
			Pattern.CASE_INSENSITIVE);

	protected static final LinkedHashMap<Class<?>, String[]> typeToArrayTypeNameMap = new LinkedHashMap<>(
			128, 0.5f);

	protected static final LinkedHashMap<String, Class<?>> arrayTypeNameToTypeMap = new LinkedHashMap<>(
			128, 0.5f);

	// 54 = RESOURCE BUSY acquiring with NOWAIT (pessimistic lock)
	public static final int PESSIMISTIC_LOCK_ERROR_CODE = 54;
	public static final int OPTIMISTIC_LOCK_ERROR_CODE = 20800;
	private static final int CONSTRAINT_VIOLATION_ERROR_CODE = 2091;
	private static final int CANNOT_INSERT_NULL_ERROR_CODE = 1400;

	protected static final String[] exportedKeysSql = {
			"SELECT USR.NAME AS OWNER, CONST.NAME AS CONSTRAINT_NAME, RCONST.NAME AS REF_CONSTRAINT_NAME, OBJ.NAME AS TABLE_NAME, COALESCE(ACOL.NAME, COL.NAME) AS COLUMN_NAME, CCOL.POS# AS POSITION, ROBJ.NAME AS REF_TABLE_NAME, COALESCE(RACOL.NAME, RCOL.NAME) AS REF_COLUMN_NAME, RCCOL.POS# AS REF_POSITION FROM SYS.CON$ CONST INNER JOIN SYS.USER$ USR ON CONST.OWNER# = USR.USER# INNER JOIN SYS.CDEF$ CDEF ON CDEF.CON# = CONST.CON# INNER JOIN SYS.CCOL$ CCOL ON CCOL.CON# = CONST.CON# INNER JOIN SYS.COL$ COL  ON (CCOL.OBJ# = COL.OBJ#) AND (CCOL.INTCOL# = COL.INTCOL#) INNER JOIN SYS.\"_CURRENT_EDITION_OBJ\" OBJ ON CCOL.OBJ# = OBJ.OBJ# LEFT JOIN SYS.ATTRCOL$ ACOL ON (CCOL.OBJ# = ACOL.OBJ#) AND (CCOL.INTCOL# = ACOL.INTCOL#) INNER JOIN SYS.CON$ RCONST ON RCONST.CON# = CDEF.RCON# INNER JOIN SYS.CCOL$ RCCOL ON RCCOL.CON# = RCONST.CON# INNER JOIN SYS.COL$ RCOL  ON (RCCOL.OBJ# = RCOL.OBJ#) AND (RCCOL.INTCOL# = RCOL.INTCOL#) INNER JOIN SYS.\"_CURRENT_EDITION_OBJ\" ROBJ ON RCCOL.OBJ# = ROBJ.OBJ# LEFT JOIN SYS.ATTRCOL$ RACOL  ON (RCCOL.OBJ# = RACOL.OBJ#) AND (RCCOL.INTCOL# = RACOL.INTCOL#) WHERE CDEF.TYPE# = 4 AND USR.NAME ",
			"SELECT C1.OWNER AS OWNER, C1.CONSTRAINT_NAME, C1.TABLE_NAME AS TABLE_NAME, A1.COLUMN_NAME AS COLUMN_NAME, C2.TABLE_NAME AS REF_TABLE_NAME, A2.COLUMN_NAME AS REF_COLUMN_NAME FROM ALL_CONSTRAINTS C1 JOIN ALL_CONSTRAINTS C2 ON C1.R_CONSTRAINT_NAME = C2.CONSTRAINT_NAME JOIN ALL_CONS_COLUMNS A1 ON C1.CONSTRAINT_NAME = A1.CONSTRAINT_NAME JOIN ALL_CONS_COLUMNS A2 ON C2.CONSTRAINT_NAME = A2.CONSTRAINT_NAME WHERE C1.CONSTRAINT_TYPE = 'R' AND C2.OWNER = C1.OWNER AND C1.OWNER " };

	static {
		typeToArrayTypeNameMap.put(Long.TYPE, new String[] { "LONG_ARRAY", "NUMBER(19,0)" });
		typeToArrayTypeNameMap.put(Long.class, new String[] { "LONG_ARRAY", "NUMBER(19,0)" });
		typeToArrayTypeNameMap.put(Integer.TYPE, new String[] { "INT_ARRAY", "NUMBER(10,0)" });
		typeToArrayTypeNameMap.put(Integer.class, new String[] { "INT_ARRAY", "NUMBER(10,0)" });
		typeToArrayTypeNameMap.put(Short.TYPE, new String[] { "SHORT_ARRAY", "NUMBER(5,0)" });
		typeToArrayTypeNameMap.put(Short.class, new String[] { "SHORT_ARRAY", "NUMBER(5,0)" });
		typeToArrayTypeNameMap.put(Byte.TYPE, new String[] { "BYTE_ARRAY", "NUMBER(3,0)" });
		typeToArrayTypeNameMap.put(Byte.class, new String[] { "BYTE_ARRAY", "NUMBER(3,0)" });
		typeToArrayTypeNameMap.put(Character.TYPE, new String[] { "CHAR_ARRAY", "CHAR" });
		typeToArrayTypeNameMap.put(Character.class, new String[] { "CHAR_ARRAY", "CHAR" });
		typeToArrayTypeNameMap.put(Boolean.TYPE, new String[] { "BOOL_ARRAY", "NUMBER(1,0)" });
		typeToArrayTypeNameMap.put(Boolean.class, new String[] { "BOOL_ARRAY", "NUMBER(1,0)" });
		typeToArrayTypeNameMap.put(Double.TYPE, new String[] { "DOUBLE_ARRAY", "NUMBER(9,9)" });
		typeToArrayTypeNameMap.put(Double.class, new String[] { "DOUBLE_ARRAY", "NUMBER(9,9)" });
		typeToArrayTypeNameMap.put(Float.TYPE, new String[] { "FLOAT_ARRAY", "NUMBER(4,4)" });
		typeToArrayTypeNameMap.put(Float.class, new String[] { "FLOAT_ARRAY", "NUMBER(4,4)" });
		typeToArrayTypeNameMap.put(String.class,
				new String[] { "STRING_ARRAY", "VARCHAR2(4000 CHAR)" });
		typeToArrayTypeNameMap.put(BigDecimal.class, new String[] { "BIG_DEC_ARRAY", "NUMBER" });
		typeToArrayTypeNameMap.put(BigInteger.class, new String[] { "BIG_INT_ARRAY", "NUMBER(38,0)" });

		// Default behavior. This is an intended "hack" for backwards compatibility.
		typeToArrayTypeNameMap.put(Object.class, new String[] { "BIG_DEC_ARRAY", "NUMBER" });

		for (Entry<Class<?>, String[]> entry : typeToArrayTypeNameMap) {
			arrayTypeNameToTypeMap.putIfNotExists(entry.getValue()[0], entry.getKey());
		}
	}

	protected final DateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

	@Autowired
	protected ILoggerHistory loggerHistory;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Autowired(optional = true)
	protected ITransactionState transactionState;

	@Override
	protected Class<?> getDriverType() {
		return OracleDriver.class;
	}

	@Override
	public int getMaxInClauseBatchThreshold() {
		return 4000;
	}

	@Override
	protected String buildDeferrableForeignKeyConstraintsSelectSQL(String[] schemaNames) {
		StringBuilder sb = new StringBuilder(
				"SELECT OWNER, TABLE_NAME, CONSTRAINT_NAME FROM ALL_CONSTRAINTS WHERE STATUS='ENABLED' AND DEFERRABLE='DEFERRABLE' AND DEFERRED='IMMEDIATE' AND CONSTRAINT_TYPE='R' AND OWNER");
		buildSchemaInClause(sb, schemaNames);
		return sb.toString();
	}

	@Override
	protected void handleRow(String schemaName, String tableName, String constraintName,
			ArrayList<String> disableConstraintsSQL, ArrayList<String> enableConstraintsSQL) {
		if (BIN_TABLE_NAME.matcher(tableName).matches()) {
			return;
		}
		String fullName = "\"" + schemaName + "\".\"" + constraintName + "\"";
		disableConstraintsSQL.add("SET CONSTRAINT " + fullName + " DEFERRED");
		enableConstraintsSQL.add("SET CONSTRAINT " + fullName + " IMMEDIATE");
	}

	@SuppressWarnings("resource")
	@SneakyThrows
	@Override
	public IList<IMap<String, String>> getExportedKeys(Connection connection, String[] schemaNames)
			{
		ArrayList<IMap<String, String>> allForeignKeys = new ArrayList<>();
		Statement stm = null;
		ResultSet allForeignKeysRS = null;
		try {
			stm = connection.createStatement();
			for (int a = 0, size = exportedKeysSql.length; a < size; a++) {
				String sql = exportedKeysSql[a];
				try {
					allForeignKeysRS = stm.executeQuery(sql + buildSchemaInClause(schemaNames));
					break;
				}
				catch (PersistenceException e) {
					if (e.getCause() instanceof SQLException) {
						if (SQLState.ACCESS_VIOLATION.getXopen()
								.equals(((SQLException) e.getCause()).getSQLState())) {
							continue;
						}
					}
					throw e;
				}
				catch (SQLException e) {
					if (SQLState.ACCESS_VIOLATION.getXopen()
							.equals(((SQLException) e.getCause()).getSQLState())) {
						continue;
					}
					throw e;
				}
			}
			if (allForeignKeysRS != null) {
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
			}
		}
		finally {
			JdbcUtil.close(stm, allForeignKeysRS);
		}
		return allForeignKeys;
	}

	@SneakyThrows
	@Override
	public ILinkedMap<String, IList<String>> getFulltextIndexes(Connection connection,
			String schemaName) {
		LinkedHashMap<String, IList<String>> fulltextIndexes = new LinkedHashMap<>();
		Statement stmt = connection.createStatement();
		ResultSet fulltextIndexesRS = null;
		try {
			fulltextIndexesRS = stmt.executeQuery(
					"SELECT A.TABLE_NAME, A.COLUMN_NAME FROM ALL_IND_COLUMNS A JOIN ALL_INDEXES B ON A.INDEX_NAME = B.INDEX_NAME AND A.TABLE_NAME = B.TABLE_NAME WHERE A.INDEX_OWNER = '"
							+ schemaName
							+ "' AND B.INDEX_TYPE = 'DOMAIN' AND B.ITYP_OWNER = 'CTXSYS' AND B.ITYP_NAME = 'CONTEXT'");
			while (fulltextIndexesRS.next()) {
				String tableName = fulltextIndexesRS.getString("TABLE_NAME");
				String columnName = fulltextIndexesRS.getString("COLUMN_NAME");

				IList<String> fulltextColumns = fulltextIndexes.get(tableName);
				if (fulltextColumns == null) {
					fulltextColumns = new ArrayList<>();
					fulltextIndexes.put(tableName, fulltextColumns);
				}
				fulltextColumns.add(columnName);
			}
		}
		finally {
			JdbcUtil.close(stmt, fulltextIndexesRS);
		}

		return fulltextIndexes;
	}

	@Override
	public boolean isSystemTable(String tableName) {
		return BIN_TABLE_NAME.matcher(tableName).matches()
				|| IDX_TABLE_NAME.matcher(tableName).matches();
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint, Connection connection) {
		// noop: releaseSavepoint(Savepoint savepoint) is not supported by Oracle10g
	}

	@Override
	public int getResourceBusyErrorCode() {
		return 54;
	}

	@Override
	public PersistenceException createPersistenceException(SQLException e, String relatedSql) {
		PersistenceException ex;
		switch (e.getErrorCode()) {
			case PESSIMISTIC_LOCK_ERROR_CODE:
				ex = new PessimisticLockException(relatedSql, e);
				break;
			case OPTIMISTIC_LOCK_ERROR_CODE:
				ex = new OptimisticLockException(relatedSql, e);
				break;
			case CANNOT_INSERT_NULL_ERROR_CODE:
				ex = new NullConstraintException(e.getMessage(), relatedSql, e);
				break;
			case CONSTRAINT_VIOLATION_ERROR_CODE:
				ex = new UniqueConstraintException(e.getMessage(), relatedSql, e);
				break;
			default:
				ex = new PersistenceException(e.getMessage(), e);
				break;
		}
		ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
		return ex;
	}

	@SneakyThrows
	@Override
	public ResultSet getIndexInfo(Connection connection, String schemaName, String tableName,
			boolean unique) {
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sql = tlObjectCollector.create(StringBuilder.class);
		try {
			sql.append(
					"SELECT AI.TABLE_NAME, AI.INDEX_NAME, INDEX_TYPE AS TYPE, COLUMN_POSITION AS ORDINAL_POSITION, COLUMN_NAME FROM ALL_INDEXES AI JOIN ALL_IND_COLUMNS AIC ON AI.INDEX_NAME = AIC.INDEX_NAME WHERE AI.OWNER = ? AND AI.TABLE_NAME = ?");
			if (unique) {
				sql.append(" AND AI.UNIQUENESS = 'UNIQUE'");
			}
			PreparedStatement prep = connection.prepareStatement(sql.toString());
			prep.setString(1, schemaName);
			prep.setString(2, tableName);
			return prep.executeQuery();
		}
		finally {
			tlObjectCollector.dispose(sql);
		}
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
	public List<String> getAllFullqualifiedSequences(Connection connection, String... schemaNames)
			{
		List<String> allSequenceNames = new ArrayList<>();

		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(
					"SELECT SEQUENCE_OWNER, SEQUENCE_NAME FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER"
							+ buildSchemaInClause(schemaNames));
			while (rs.next()) {
				String schemaName = rs.getString("SEQUENCE_OWNER");
				String sequenceName = rs.getString("SEQUENCE_NAME");
				if (!BIN_TABLE_NAME.matcher(sequenceName).matches()
						&& !IDX_TABLE_NAME.matcher(sequenceName).matches()) {
					allSequenceNames.add(schemaName + '.' + sequenceName);
				}
			}
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			JdbcUtil.close(stmt, rs);
		}

		return allSequenceNames;
	}

	@SneakyThrows
	@Override
	public List<String> getAllFullqualifiedTableNames(Connection connection, String... schemaNames)
			{
		List<String> allTableNames = new ArrayList<>();

		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT OWNER, TABLE_NAME FROM ALL_ALL_TABLES WHERE OWNER"
					+ buildSchemaInClause(schemaNames));
			while (rs.next()) {
				String schemaName = rs.getString("OWNER");
				String tableName = rs.getString("TABLE_NAME");
				if (!BIN_TABLE_NAME.matcher(tableName).matches()
						&& !IDX_TABLE_NAME.matcher(tableName).matches()) {
					allTableNames.add(schemaName + "." + tableName);
				}
			}
		}
		finally {
			JdbcUtil.close(stmt, rs);
		}

		return allTableNames;
	}

	@SneakyThrows
	@Override
	public List<String> getAllFullqualifiedViews(Connection connection, String... schemaNames)
			{
		List<String> allViewNames = new ArrayList<>();

		Statement stmt = null;
		ResultSet rs = null;
		try {
			for (String schemaName : schemaNames) {
				rs = connection.getMetaData().getTables(null, schemaName, null, new String[] { "VIEW" });

				while (rs.next()) {
					// String schemaName = rs.getString("TABLE_SCHEM");
					String viewName = rs.getString("TABLE_NAME");
					if (!BIN_TABLE_NAME.matcher(viewName).matches()
							&& !IDX_TABLE_NAME.matcher(viewName).matches()) {
						allViewNames.add(schemaName + "." + viewName);
					}
				}
			}
		}
		finally {
			JdbcUtil.close(stmt, rs);
		}

		return allViewNames;
	}

	@SneakyThrows
	@Override
	public IList<IColumnEntry> getAllFieldsOfTable(Connection connection, String fqTableName)
			{
		String[] names = sqlBuilder.getSchemaAndTableName(fqTableName);
		ResultSet tableColumnsRS = connection.getMetaData().getColumns(null, names[0], names[1], null);
		try {
			ArrayList<IColumnEntry> columns = new ArrayList<>();
			columns.add(new ColumnEntry("ROWID", -1, Object.class, null, false, 0, false));

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

				ColumnEntry entry = new ColumnEntry(fieldName, columnIndex, javaType, typeName, nullable,
						radix, true);
				columns.add(entry);
			}
			return columns;
		}
		finally {
			JdbcUtil.close(tableColumnsRS);
		}
	}

	@Override
	public boolean isEmptyStringAsNullStored(IFieldMetaData field) {
		String originalTypeName = field.getOriginalTypeName();
		if ("VARCHAR2".equals(originalTypeName) || "VARCHAR".equals(originalTypeName)) {
			return true;
		}
		return false;
	}

	@Override
	public String prepareCommand(String sqlCommand) {
		return sqlCommand;
	}

	@Override
	public SelectPosition getLimitPosition() {
		return SelectPosition.AS_WHERE_CLAUSE;
	}

	@Override
	public int getColumnCountForLinkTable() {
		return 3;
	}

	@Override
	public void preProcessConnection(Connection connection, String[] schemaNames,
			boolean forcePreProcessing) {
		super.preProcessConnection(connection, schemaNames, forcePreProcessing);
		try {
			// since Oracle 11.2.0.4.0 there is an event '60025' that needs to be explicitly activated
			// within each session. this is needed to solve open LOB handle issues leading to continuously
			// growing TEMP tablespace until a session/connection is released since that does not happen
			// often for pooled connections it leads then to a fatal 'ORA-01652: unable to extend temp
			// segment by 128 in tablespace TEMP'
			Statement stm = connection.createStatement();
			try {
				stm.execute("ALTER SESSION SET EVENTS '60025 trace name context forever'");
			}
			finally {
				JdbcUtil.close(stm);
			}
		}
		catch (PersistenceException e) {
			if (e.getCause() instanceof SQLException
					&& ((SQLException) e.getCause()).getErrorCode() == 1031) {
				// ORA-01031: insufficient privileges
				return;
			}
			throw e;
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Class<?>[] getConnectionInterfaces(Connection connection) {
		if (connection instanceof OracleConnection) {
			return new Class<?>[] { OracleConnection.class };
		}
		return super.getConnectionInterfaces(connection);
	}
}
