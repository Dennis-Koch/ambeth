package com.koch.ambeth.persistence.oracle;

/*-
 * #%L
 * jambeth-persistence-oracle11-test
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
import java.sql.Statement;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.PermissionGroup;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.AbstractConnectionTestDialect;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.log.PersistenceWarnUtil;
import com.koch.ambeth.persistence.oracle.RandomUserScript.RandomUserModule;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class Oracle10gTestDialect extends AbstractConnectionTestDialect {
	public static final String ROOT_DATABASE_USER = "ambeth.root.database.user";

	public static final String ROOT_DATABASE_PASS = "ambeth.root.database.pass";

	@LogInstance
	private ILogger log;

	@Autowired
	protected ILoggerHistory loggerHistory;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Property(name = PersistenceConfigurationConstants.AutoArrayTypes, defaultValue = "true")
	protected boolean autoArrayTypes;

	@Property(name = PersistenceConfigurationConstants.AutoIndexForeignKeys, defaultValue = "false")
	protected boolean autoIndexForeignKeys;

	@Property(name = ROOT_DATABASE_USER, defaultValue = "sys as sysdba")
	protected String rootDatabaseUser;

	@Property(name = ROOT_DATABASE_PASS, defaultValue = "developer")
	protected String rootDatabasePass;

	@Override
	public boolean createTestUserIfSupported(Throwable reason, String userName, String userPassword,
			IProperties testProps) throws SQLException {
		if (!(reason instanceof SQLException)) {
			return false;
		}
		if (((SQLException) reason).getErrorCode() != 1017) // ORA-01017: invalid username/password;
																												// logon denied
		{
			return false;
		}
		// try to recover by trying to create the necessary user with the default credentials of sys
		Properties createUserProps = new Properties(testProps);
		createUserProps.put(RandomUserScript.SCRIPT_IS_CREATE, "true");
		createUserProps.put(RandomUserScript.SCRIPT_USER_NAME, userName);
		createUserProps.put(RandomUserScript.SCRIPT_USER_PASS, userPassword);

		createUserProps.put(PersistenceJdbcConfigurationConstants.DatabaseUser, rootDatabaseUser);
		createUserProps.put(PersistenceJdbcConfigurationConstants.DatabasePass, rootDatabasePass);
		try (IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(createUserProps)) {
			bootstrapContext.createService("randomUser", RandomUserModule.class, IocModule.class);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		return true;
	}

	@Override
	public void dropCreatedTestUser(String userName, String userPassword, IProperties testProps)
			throws SQLException {
		Properties createUserProps = new Properties(testProps);
		createUserProps.put(RandomUserScript.SCRIPT_IS_CREATE, "false");
		createUserProps.put(RandomUserScript.SCRIPT_USER_NAME, userName);
		createUserProps.put(RandomUserScript.SCRIPT_USER_PASS, userPassword);

		createUserProps.put(PersistenceJdbcConfigurationConstants.DatabaseUser, rootDatabaseUser);
		createUserProps.put(PersistenceJdbcConfigurationConstants.DatabasePass, rootDatabasePass);
		try (IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(createUserProps)) {
			bootstrapContext.createService("randomUser", RandomUserModule.class, IocModule.class);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void preProcessConnectionForTest(Connection connection, String[] schemaNames,
			boolean forcePreProcessing) {
		try {
			handleIndices(connection);

			if (autoArrayTypes) {
				handleArrayTypes(connection);
			}
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void handleIndices(Connection connection) throws SQLException {
		Statement stm = connection.createStatement();
		Statement createIndexStm = null;
		ResultSet rs = null;
		try {
			rs = stm.executeQuery("select table_name, constraint_name,cname1"
					+ "|| nvl2(cname2,','||cname2,null) || nvl2(cname3,','||cname3,null) || nvl2(cname4,','||cname4,null)"
					+ "|| nvl2(cname5,','||cname5,null) || nvl2(cname6,','||cname6,null) || nvl2(cname7,','||cname7,null) || nvl2(cname8,','||cname8,null)"
					+ " columns" + " from ( select b.table_name," + " b.constraint_name,"
					+ " max(decode( position, 1, column_name, null )) cname1,"
					+ " max(decode( position, 2, column_name, null )) cname2, max(decode( position, 3, column_name, null )) cname3,"
					+ " max(decode( position, 4, column_name, null )) cname4, max(decode( position, 5, column_name, null )) cname5,"
					+ " max(decode( position, 6, column_name, null )) cname6, max(decode( position, 7, column_name, null )) cname7,"
					+ " max(decode( position, 8, column_name, null )) cname8, count(*) col_cnt from (select substr(table_name,1,30) table_name,"
					+ " substr(constraint_name,1,30) constraint_name, substr(column_name,1,30) column_name, position from user_cons_columns ) a,"
					+ " user_constraints b where a.constraint_name = b.constraint_name and b.constraint_type = 'R'"
					+ " group by b.table_name, b.constraint_name ) cons where col_cnt > ALL ( select count(*) from user_ind_columns i"
					+ " where i.table_name = cons.table_name and i.column_name in (cname1, cname2, cname3, cname4, cname5, cname6, cname7, cname8 )"
					+ " and i.column_position <= cons.col_cnt group by i.index_name )");

			int maxIndexLength = -1;
			boolean constraintFound = false;
			while (rs.next()) {
				constraintFound = true;
				String tableName = rs.getString("table_name");
				String constraintName = rs.getString("constraint_name");
				String columns = rs.getString("columns");

				if (autoIndexForeignKeys) {
					if (createIndexStm == null) {
						createIndexStm = connection.createStatement();
						maxIndexLength = connection.getMetaData().getMaxTableNameLength();
					}
					String indexName = "IX_" + constraintName;
					if (indexName.length() > maxIndexLength) {
						// Index has to be truncated and randomized to 'ensure' uniqueness
						indexName = indexName.substring(0, maxIndexLength - 2) + (int) (Math.random() * 98 + 1);
					}
					String sql = "CREATE INDEX \"" + indexName + "\" ON \"" + tableName + "\" (\"" + columns
							+ "\")";
					createIndexStm.addBatch(sql);
				}
				else {
					if (log.isWarnEnabled()) {
						PersistenceWarnUtil.logWarnOnce(log, loggerHistory, connection,
								"No index for constraint '" + constraintName + "' on table '" + tableName
										+ "' for column '" + columns + "' found");
					}
				}
			}
			if (createIndexStm != null) {
				createIndexStm.executeBatch();
				if (log.isDebugEnabled()) {
					log.debug(
							"Runtime creation of indexes for foreign key constraints successful. This has been done because '"
									+ PersistenceConfigurationConstants.AutoIndexForeignKeys
									+ "' has been specified to 'true'");
				}
			}
			if (constraintFound && !autoIndexForeignKeys) {
				if (log.isWarnEnabled()) {
					PersistenceWarnUtil.logWarnOnce(log, loggerHistory, connection,
							"At least one missing index found on foreign key constraints. Maybe you should specify '"
									+ PersistenceConfigurationConstants.AutoIndexForeignKeys
									+ "=true' to allow auto-indexing at runtime or review your database schema");
				}
			}
		}
		finally {
			JdbcUtil.close(createIndexStm);
			JdbcUtil.close(stm, rs);
		}
	}

	protected void handleArrayTypes(Connection connection) throws SQLException {
		Statement stm = connection.createStatement();
		ResultSet rs = null;
		try {
			rs = stm.executeQuery(
					"SELECT object_type, object_name FROM user_objects WHERE object_type IN ('TYPE')");

			LinkedHashMap<String, Class<?>> nameToArrayTypeMap = new LinkedHashMap<>();
			for (Entry<Class<?>, String[]> entry : Oracle10gDialect.typeToArrayTypeNameMap) {
				Class<?> type = entry.getKey();
				nameToArrayTypeMap.put(entry.getValue()[0], type);
			}

			while (rs.next()) {
				String typeName = rs.getString("object_name");
				nameToArrayTypeMap.remove(typeName);
			}
			JdbcUtil.close(rs);
			rs = null;
			StringBuilder sb = new StringBuilder();
			for (Entry<String, Class<?>> entry : nameToArrayTypeMap) {
				String necessaryTypeName = entry.getKey();

				sb.setLength(0);
				;
				sb.append("CREATE TYPE ").append(necessaryTypeName).append(" AS VARRAY(4000) OF ");
				String arrayTypeName = Oracle10gDialect.typeToArrayTypeNameMap.get(entry.getValue())[1];
				sb.append(arrayTypeName);
				stm.execute(sb.toString());
			}
		}
		finally {
			JdbcUtil.close(stm, rs);
		}
	}

	@Override
	public void resetStatementCache(Connection connection) {
		Statement stm = null;
		try {
			stm = connection.createStatement();
			stm.execute("alter system flush shared_pool");
		}
		catch (PersistenceException e) {
			if (e.getCause() instanceof SQLException
					&& "42000".equals(((SQLException) e.getCause()).getSQLState())) {
				return;
			}
			throw e;
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			JdbcUtil.close(stm);
		}
	}

	@Override
	public boolean isEmptySchema(Connection connection) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT tname FROM tab");
			while (rs.next()) {
				if (!Oracle10gDialect.BIN_TABLE_NAME.matcher(rs.getString("tname")).matches()
						&& !Oracle10gDialect.IDX_TABLE_NAME.matcher(rs.getString("tname")).matches()) {
					return false;
				}
			}
			rs.close();
			rs = stmt.executeQuery(
					"SELECT object_type, object_name FROM user_objects WHERE object_type IN ('FUNCTION', 'INDEX', 'PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'SEQUENCE', 'TABLE', 'TYPE', 'VIEW')");
			return !rs.next();
		}
		finally {
			JdbcUtil.close(stmt, rs);
		}
	}

	@Override
	public String[] createOptimisticLockTrigger(Connection connection, String fqTableName)
			throws SQLException {
		if (Oracle10gDialect.BIN_TABLE_NAME.matcher(fqTableName).matches()
				|| Oracle10gDialect.IDX_TABLE_NAME.matcher(fqTableName).matches()) {
			return new String[0];
		}
		String[] names = sqlBuilder.getSchemaAndTableName(fqTableName);
		ArrayList<String> tableColumns = new ArrayList<>();
		ResultSet tableColumnsRS = connection.getMetaData().getColumns(null, names[0], names[1], null);
		try {
			while (tableColumnsRS.next()) {
				String columnName = tableColumnsRS.getString("COLUMN_NAME");
				if (columnName.equalsIgnoreCase(PermissionGroup.permGroupIdNameOfData)) {
					continue;
				}
				int columnType = tableColumnsRS.getInt("DATA_TYPE");
				if (java.sql.Types.CLOB == columnType || java.sql.Types.BLOB == columnType) {
					// ORA-25006: cannot specify this column in UPDATE OF clause
					// lobs have a lob locator as a pointer to the internal technical lob storage. the lob
					// locator is never changed when a lob is initialized or
					// updated
					continue;
				}
				tableColumns.add(columnName);
			}
		}
		finally {
			JdbcUtil.close(tableColumnsRS);
		}
		int maxProcedureNameLength = connection.getMetaData().getMaxProcedureNameLength();
		AppendableStringBuilder sb = new AppendableStringBuilder();
		String triggerName = ormPatternMatcher.buildOptimisticLockTriggerFromTableName(fqTableName,
				maxProcedureNameLength);
		sb.append("create or replace TRIGGER \"").append(triggerName);
		sb.append("\" BEFORE UPDATE");
		if (!tableColumns.isEmpty()) {
			sb.append(" OF ");
			for (int a = 0, size = tableColumns.size(); a < size; a++) {
				if (a > 0) {
					sb.append(',');
				}
				connectionDialect.escapeName(tableColumns.get(a), sb);
			}
		}
		sb.append(" ON \"").append(names[1]).append("\" FOR EACH ROW");
		sb.append(" BEGIN");
		sb.append(" if( :new.\"VERSION\" <= :old.\"VERSION\" ) then");
		sb.append(" raise_application_error( -");
		sb.append(Integer.toString(Oracle10gDialect.OPTIMISTIC_LOCK_ERROR_CODE))
				.append(", 'Optimistic Lock Exception');");
		sb.append(" end if;");
		sb.append(" END;");
		return new String[] { sb.toString() };
	}

	@Override
	protected boolean isTableNameToIgnore(String tableName) {
		if (Oracle10gDialect.BIN_TABLE_NAME.matcher(tableName).matches()) {
			return true;
		}
		return false;
	}

	@Override
	protected IList<String> queryForAllTables(Connection connection) throws SQLException {
		return connectionDialect.queryDefault(connection, "FULL_NAME",
				"SELECT USER || '.' || TNAME FULL_NAME FROM DUAL, TAB T JOIN COLS C ON T.TNAME = C.TABLE_NAME WHERE T.TABTYPE='TABLE' AND C.COLUMN_NAME='VERSION'");
	}

	@Override
	protected IList<String> queryForAllTriggers(Connection connection) throws SQLException {
		return connectionDialect.queryDefault(connection, "TRIGGER_NAME",
				"SELECT TRIGGER_NAME FROM ALL_TRIGGERS");
	}

	@Override
	protected IList<String> queryForAllPermissionGroupNeedingTables(Connection connection)
			throws SQLException {
		return connectionDialect.queryDefault(connection, "TNAME",
				"SELECT TNAME FROM TAB T JOIN COLS C ON T.TNAME = C.TABLE_NAME WHERE T.TABTYPE='TABLE' AND C.COLUMN_NAME='"
						+ PermissionGroup.permGroupIdNameOfData + "'");
	}

	@Override
	protected IList<String> queryForAllPotentialPermissionGroups(Connection connection)
			throws SQLException {
		return connectionDialect.queryDefault(connection, "PERM_GROUP_NAME",
				"SELECT TNAME AS PERM_GROUP_NAME FROM TAB T");
	}

	@Override
	public String[] createPermissionGroup(Connection connection, String tableName)
			throws SQLException {
		int maxProcedureNameLength = connection.getMetaData().getMaxProcedureNameLength();
		String permissionGroupName = ormPatternMatcher.buildPermissionGroupFromTableName(tableName,
				maxProcedureNameLength);
		String pkName;
		Matcher matcher = Pattern.compile("(?:.*\\.)?([^\\.]+)").matcher(permissionGroupName);
		if (matcher.matches()) {
			pkName = matcher.group(1) + "_PK";
		}
		else {
			pkName = tableName + "_PK";
		}
		ArrayList<String> sql = new ArrayList<>();

		sql.add("CREATE TABLE \"" + permissionGroupName + "\" "//
				+ "(\"" + PermissionGroup.userIdName + "\" VARCHAR2(64 CHAR) NOT NULL,"//
				+ "\"" + PermissionGroup.permGroupIdName + "\" NUMBER(18,0) NOT NULL,"//
				+ "\"" + PermissionGroup.readPermColumName + "\" NUMBER(1,0),"//
				+ "\"" + PermissionGroup.updatePermColumName + "\" NUMBER(1,0),"//
				+ "\"" + PermissionGroup.deletePermColumName + "\" NUMBER(1,0),"//
				+ "CONSTRAINT \"" + pkName + "\" PRIMARY KEY ("//
				+ "\"" + PermissionGroup.userIdName + "\",\"" + PermissionGroup.permGroupIdName + "\""//
				+ ") USING INDEX )");

		sql.add("CREATE INDEX \"" + permissionGroupName + "_IDX\"" + " ON \"" + permissionGroupName
				+ "\" (\"" + PermissionGroup.permGroupIdName + "\")");

		sql.add("CREATE SEQUENCE \"" + permissionGroupName
				+ "_SEQ\" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE");

		// PreparedStatement pstm = null;
		// ResultSet rs = null;
		// try
		// {
		// pstm = conn
		// .prepareStatement("SELECT T.TNAME as TNAME FROM TAB T LEFT OUTER JOIN COLS C ON T.TNAME =
		// C.TABLE_NAME WHERE C.COLUMN_NAME=? AND T.TNAME IN (?)");
		// pstm.setString(1, PermissionGroup.permGroupIdNameOfData);
		// pstm.setString(2, tableName);
		// rs = pstm.executeQuery();
		// if (!rs.next())
		// {
		// sql.add("ALTER TABLE " + tableName + " ADD \"" + PermissionGroup.permGroupIdNameOfData + "\"
		// NUMBER(18,0)");
		// }
		// }
		// finally
		// {
		// JdbcUtil.close(pstm, rs);
		// }

		return sql.toArray(String.class);
	}

	@Override
	public void dropAllSchemaContent(Connection connection, String schemaName) {
		Statement stmt = null, stmt2 = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			stmt2 = connection.createStatement();
			rs = stmt.executeQuery("SELECT TNAME, TABTYPE FROM TAB");
			while (rs.next()) {
				String tableName = rs.getString(1);
				if (Oracle10gDialect.BIN_TABLE_NAME.matcher(tableName).matches()
						|| Oracle10gDialect.IDX_TABLE_NAME.matcher(tableName).matches()) {
					continue;
				}
				String tableType = rs.getString(2);
				if ("VIEW".equalsIgnoreCase(tableType)) {
					stmt2.execute("DROP VIEW " + escapeName(schemaName, tableName) + " CASCADE CONSTRAINTS");
				}
				else if ("TABLE".equalsIgnoreCase(tableType)) {
					stmt2.execute("DROP TABLE " + escapeName(schemaName, tableName) + " CASCADE CONSTRAINTS");
				}
				else if ("SYNONYM".equalsIgnoreCase(tableType)) {
					stmt2.execute("DROP SYNONYM " + escapeName(schemaName, tableName));
				}
			}
			JdbcUtil.close(rs);
			rs = stmt.executeQuery(
					"SELECT object_type, object_name FROM user_objects WHERE object_type IN ('FUNCTION', 'INDEX', 'PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'SEQUENCE', 'SYNONYM', 'TABLE', 'TYPE', 'VIEW')");
			while (rs.next()) {
				String objectType = rs.getString("object_type");
				String objectName = rs.getString("object_name");
				if (Oracle10gDialect.BIN_TABLE_NAME.matcher(objectName).matches()
						|| Oracle10gDialect.IDX_TABLE_NAME.matcher(objectName).matches()) {
					continue;
				}
				stmt2.execute("DROP " + objectType + " " + escapeName(schemaName, objectName));
			}
			stmt2.execute("PURGE RECYCLEBIN");
		}
		catch (SQLException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			JdbcUtil.close(stmt, rs);
			JdbcUtil.close(stmt2);
		}
	}
}
