package com.koch.ambeth.persistence.sqlite;

/*-
 * #%L
 * jambeth-persistence-sqlite-test
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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.PermissionGroup;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.jdbc.AbstractConnectionTestDialect;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.sqlite.RandomUserScript.RandomUserModule;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class SQLiteTestDialect extends AbstractConnectionTestDialect {
	public static final String ROOT_DATABASE_USER = "ambeth.root.database.user";

	public static final String ROOT_DATABASE_PASS = "ambeth.root.database.pass";

	@Property(name = ROOT_DATABASE_USER, defaultValue = "postgres")
	protected String rootDatabaseUser;

	@Property(name = ROOT_DATABASE_PASS, defaultValue = "developer")
	protected String rootDatabasePass;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Override
	public boolean createTestUserIfSupported(Throwable reason, String userName, String userPassword,
			IProperties testProps) throws SQLException {
		if (!(reason instanceof SQLException)) {
			return false;
		}
		if (!"28P01".equals(((SQLException) reason).getSQLState()) // INVALID PASSWORD, FATAL: password
																																// authentication failed for user
																																// "xxx"
				&& !"3D000".equals(((SQLException) reason).getSQLState()) // FATAL: database "xxx" does not
																																	// exist
				&& !"28000".equals(((SQLException) reason).getSQLState()) // INVALID AUTHORIZATION
																																	// SPECIFICATION undefined role
																																	// tried to access the server
		) {
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
		IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(createUserProps);
		try {
			bootstrapContext.createService("randomUser", RandomUserModule.class, IocModule.class);
		}
		finally {
			bootstrapContext.dispose();
		}
	}

	@Override
	public void preProcessConnectionForTest(Connection connection, String[] schemaNames,
			boolean forcePreProcessing) {
		// intended blank
	}

	@Override
	public boolean isEmptySchema(Connection connection) throws SQLException {
		DatabaseMetaData metaData = connection.getMetaData();
		ResultSet tables = metaData.getTables(null, null, null, null);
		try {
			return !tables.next();
		}
		finally {
			tables.close();
		}
	}

	@Override
	public String[] createOptimisticLockTrigger(Connection connection, String fqTableName)
			throws SQLException {
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
		sb.append("create TRIGGER if not exists \"").append(triggerName);
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
		sb.append(" WHEN (new.\"VERSION\" <= old.\"VERSION\") BEGIN");
		sb.append(" SELECT RAISE(ABORT, 'Optimistic Lock Exception');");
		sb.append(" END;");
		return new String[] { sb.toString() };
	}

	@Override
	protected IList<String> queryForAllTables(Connection connection) throws SQLException {
		return connectionDialect.queryDefault(connection, "FULL_NAME",
				"SELECT tbl_name AS FULL_NAME FROM sqlite_master where type='table'");
	}

	@Override
	protected IList<String> queryForAllTriggers(Connection connection) throws SQLException {
		return connectionDialect.queryDefault(connection, "FULL_NAME",
				"SELECT tbl_name AS FULL_NAME FROM sqlite_master where type='trigger'");
	}

	@Override
	protected IList<String> queryForAllPermissionGroupNeedingTables(Connection connection)
			throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT name, sql FROM sqlite_master where type='table'");
			ArrayList<String> result = new ArrayList<>();
			while (rs.next()) {
				String tableName = rs.getString("name");
				String sql = rs.getString("sql");
				Pattern parseDefinitions = Pattern.compile("[^\\(]+\\((.+)\\)");
				Matcher matcher = parseDefinitions.matcher(sql);
				if (!matcher.matches()) {
					throw new IllegalStateException("SQL table create not parseable: '" + sql + "'");
				}
				String definitions = matcher.group(1).trim();
				for (String definition : definitions.split(",")) {
					definition = definition.trim();
					String firstWord = definition.split(" ")[0];
					if (firstWord.equals(PermissionGroup.permGroupIdNameOfData)
							|| firstWord.equals("\"" + PermissionGroup.permGroupIdNameOfData + "\"")) {
						result.add(tableName);
						break;
					}
				}
			}
			return result;
		}
		finally {
			JdbcUtil.close(stmt, rs);
		}
	}

	@Override
	protected IList<String> queryForAllPotentialPermissionGroups(Connection connection)
			throws SQLException {
		return connectionDialect.queryDefault(connection, "FULL_NAME",
				"SELECT tbl_name AS FULL_NAME FROM sqlite_master where type='table'");
	}

	@Override
	public String[] createPermissionGroup(Connection connection, String tableName)
			throws SQLException {
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
		// int maxProcedureNameLength = connection.getMetaData().getMaxProcedureNameLength();
		// String permissionGroupName = ormPatternMatcher.buildPermissionGroupFromTableName(tableName,
		// maxProcedureNameLength);
		// String pkName;
		// Matcher matcher = Pattern.compile("(?:.*\\.)?([^\\.]+)").matcher(permissionGroupName);
		// if (matcher.matches())
		// {
		// pkName = matcher.group(1) + "_PK";
		// }
		// else
		// {
		// pkName = tableName + "_PK";
		// }
		// ArrayList<String> sql = new ArrayList<String>();
		//
		// sql.add("CREATE TABLE \"" + permissionGroupName + "\" "//
		// + "(\"" + PermissionGroup.userIdName + "\" VARCHAR2(64 CHAR) NOT NULL,"//
		// + "\"" + PermissionGroup.permGroupIdName + "\" NUMBER(18,0) NOT NULL,"//
		// + "\"" + PermissionGroup.readPermColumName + "\" NUMBER(1,0),"//
		// + "\"" + PermissionGroup.updatePermColumName + "\" NUMBER(1,0),"//
		// + "\"" + PermissionGroup.deletePermColumName + "\" NUMBER(1,0),"//
		// + "CONSTRAINT \"" + pkName + "\" PRIMARY KEY ("//
		// + "\"" + PermissionGroup.userIdName + "\",\"" + PermissionGroup.permGroupIdName + "\""//
		// + ") USING INDEX )");
		//
		// sql.add("CREATE INDEX \"" + permissionGroupName + "_IDX\"" + " ON \"" + permissionGroupName +
		// "\" (\"" + PermissionGroup.permGroupIdName + "\")");
		//
		// // PreparedStatement pstm = null;
		// // ResultSet rs = null;
		// // try
		// // {
		// // pstm = conn
		// // .prepareStatement("SELECT T.TNAME as TNAME FROM TAB T LEFT OUTER JOIN COLS C ON T.TNAME =
		// C.TABLE_NAME WHERE C.COLUMN_NAME=? AND T.TNAME IN (?)");
		// // pstm.setString(1, PermissionGroup.permGroupIdNameOfData);
		// // pstm.setString(2, tableName);
		// // rs = pstm.executeQuery();
		// // if (!rs.next())
		// // {
		// // sql.add("ALTER TABLE " + tableName + " ADD \"" + PermissionGroup.permGroupIdNameOfData +
		// "\" NUMBER(18,0)");
		// // }
		// // }
		// // finally
		// // {
		// // JdbcUtil.close(pstm, rs);
		// // }
		//
		// return sql.toArray(String.class);
	}

	@Override
	public void dropAllSchemaContent(Connection connection, String schemaName) {
		Statement stmt = null, stmt2 = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();

			stmt.execute("PRAGMA writable_schema = 1");
			stmt.execute("delete from sqlite_master where type in ('table', 'index', 'trigger')");
			stmt.execute("PRAGMA writable_schema = 0");

			rs = stmt.executeQuery("SELECT * from sqlite_master");
			int columnCount = rs.getMetaData().getColumnCount();
			for (int a = 0, size = columnCount; a < size; a++) {
				System.out.print("\t" + rs.getMetaData().getColumnName(a + 1));
			}
			System.out.println();
			while (rs.next()) {
				for (int a = 0, size = columnCount; a < size; a++) {
					System.out.print("\t" + rs.getString(a + 1));
				}
				System.out.println();
			}

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
