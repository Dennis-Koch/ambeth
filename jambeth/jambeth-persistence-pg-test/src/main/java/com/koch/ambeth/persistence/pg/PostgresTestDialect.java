package com.koch.ambeth.persistence.pg;

/*-
 * #%L
 * jambeth-persistence-pg-test
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.IColumnEntry;
import com.koch.ambeth.persistence.PermissionGroup;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.jdbc.AbstractConnectionTestDialect;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.pg.RandomUserScript.RandomUserModule;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class PostgresTestDialect extends AbstractConnectionTestDialect {
	public static final String ROOT_DATABASE_USER = "ambeth.root.database.user";

	public static final String ROOT_DATABASE_PASS = "ambeth.root.database.pass";

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Property(name = ROOT_DATABASE_USER, defaultValue = "postgres")
	protected String rootDatabaseUser;

	@Property(name = ROOT_DATABASE_PASS, defaultValue = "developer")
	protected String rootDatabasePass;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseName, mandatory = false)
	protected String databaseName;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName)
	protected String schemaName;

	protected String[] schemaNames;

	protected final HashSet<String> ignoredTables = new HashSet<>();

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();
		schemaNames = connectionDialect.toDefaultCase(schemaName).split("[:;]");
	}

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
		createUserProps.put(RandomUserScript.SCRIPT_DATABASE_NAME, databaseName);
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
	public void preStructureRebuild(Connection connection) throws SQLException {
		super.preStructureRebuild(connection);

		Statement stm = null;
		try {
			stm = connection.createStatement();
			for (String schemaName : schemaNames) {
				stm.execute("CREATE SCHEMA IF NOT EXISTS \"" + schemaName + "\"");
				// stm.execute("CREATE DOMAIN \"" + schemaName + "\".lo AS oid");
				stm.execute("CREATE EXTENSION IF NOT EXISTS lo SCHEMA \"" + schemaName + "\"");
			}
			stm.execute("SET SCHEMA '" + schemaNames[0] + "'");
		}
		finally {
			JdbcUtil.close(stm);
		}
	}

	@Override
	public void preProcessConnectionForTest(Connection connection, String[] schemaNames,
			boolean forcePreProcessing) {
		// intended blank
	}

	@Override
	public boolean isEmptySchema(Connection connection) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();

			rs = stmt.executeQuery(
					"SELECT count(*) FROM pg_class c INNER JOIN pg_namespace n ON c.relnamespace=n.oid WHERE n.nspname='"
							+ schemaNames[0] + "'");
			rs.next();
			return rs.getInt(1) == 0;
		}
		finally {
			JdbcUtil.close(stmt, rs);
		}
	}

	@Override
	public String[] createAdditionalTriggers(Connection connection, String fqTableName)
			throws SQLException {
		IList<IColumnEntry> allFieldsOfTable =
				connectionDialect.getAllFieldsOfTable(connection, fqTableName);
		ArrayList<String> sql = new ArrayList<>();
		String[] schemaAndTableName = sqlBuilder.getSchemaAndTableName(fqTableName);
		for (IColumnEntry columnEntry : allFieldsOfTable) {
			if (!PostgresDialect.isBLobColumnName(columnEntry.getTypeName())) {
				continue;
			}
			String triggerName = schemaAndTableName[1] + "_lob_" + columnEntry.getFieldName();
			{
				AppendableStringBuilder sb = new AppendableStringBuilder();

				sb.append("CREATE TRIGGER ").append(triggerName);
				sb.append(" BEFORE UPDATE OF ");
				connectionDialect.escapeName(columnEntry.getFieldName(), sb);
				sb.append(" OR DELETE ");
				sb.append(" ON \"").append(schemaAndTableName[1])
						.append("\" FOR EACH ROW EXECUTE PROCEDURE lo_manage(");
				connectionDialect.escapeName(columnEntry.getFieldName(), sb);
				sb.append(")");
				sql.add(sb.toString());
			}
			{
				AppendableStringBuilder sb = new AppendableStringBuilder();

				sb.append("CREATE TRIGGER ").append(triggerName).append("_t");
				sb.append(" BEFORE TRUNCATE");
				sb.append(" ON \"").append(schemaAndTableName[1]).append("\" EXECUTE PROCEDURE lo_manage(");
				connectionDialect.escapeName(columnEntry.getFieldName(), sb);
				sb.append(")");
				sql.add(sb.toString());
			}
		}
		return sql.toArray(String.class);
	}

	@Override
	public String[] createOptimisticLockTrigger(Connection connection, String fqTableName)
			throws SQLException {
		if (PostgresDialect.BIN_TABLE_NAME.matcher(fqTableName).matches()
				|| PostgresDialect.IDX_TABLE_NAME.matcher(fqTableName).matches()) {
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
		String triggerName = ormPatternMatcher.buildOptimisticLockTriggerFromTableName(fqTableName,
				maxProcedureNameLength);

		String functionName = "f_" + triggerName;
		String[] sql = new String[2];
		{
			AppendableStringBuilder sb = new AppendableStringBuilder();
			sb.append("CREATE OR REPLACE FUNCTION ").append(functionName)
					.append("() RETURNS TRIGGER AS $").append(functionName).append("$\n");
			sb.append(" BEGIN\n");
			sb.append("  IF NEW.\"").append("VERSION").append("\" <= OLD.\"").append("VERSION")
					.append("\" THEN\n");
			sb.append("  RAISE EXCEPTION '")
					.append(Integer.toString(PostgresDialect.getOptimisticLockErrorCode()))
					.append(" Optimistic Lock Exception';\n");
			sb.append("  END IF;\n");
			sb.append("  RETURN NEW;");
			sb.append(" END;\n");
			sb.append("$").append(functionName).append("$ LANGUAGE plpgsql COST 1");
			sql[0] = sb.toString();
		}
		{
			AppendableStringBuilder sb = new AppendableStringBuilder();

			sb.append("CREATE TRIGGER \"").append(triggerName);
			sb.append("\" BEFORE UPDATE");
			if (tableColumns.size() > 0) {
				sb.append(" OF ");
				for (int a = 0, size = tableColumns.size(); a < size; a++) {
					if (a > 0) {
						sb.append(',');
					}
					sqlBuilder.escapeName(tableColumns.get(a), sb);
				}
			}
			sb.append(" ON \"").append(names[1]).append("\" FOR EACH ROW EXECUTE PROCEDURE ")
					.append(functionName).append("()");
			sql[1] = sb.toString();
		}
		return sql;
	}

	@Override
	protected boolean isTableNameToIgnore(String tableName) {
		if (PostgresDialect.BIN_TABLE_NAME.matcher(tableName).matches()) {
			return true;
		}
		return false;
	}

	@Override
	protected IList<String> queryForAllTables(Connection connection) throws SQLException {
		return connectionDialect.queryDefault(connection, "FULL_NAME",
				"SELECT DISTINCT n.nspname || '.' || c.relname AS FULL_NAME FROM pg_trigger t JOIN pg_class c ON t.tgrelid=c.oid JOIN pg_namespace n ON c.relnamespace=n.oid WHERE n.nspname='"
						+ schemaNames[0] + "'");
	}

	@Override
	protected IList<String> queryForAllTriggers(Connection connection) throws SQLException {
		return connectionDialect.queryDefault(connection, "TRIGGER_NAME",
				"SELECT t.tgname AS TRIGGER_NAME FROM pg_trigger t");
	}

	@Override
	protected IList<String> queryForAllPermissionGroupNeedingTables(Connection connection)
			throws SQLException {
		return connectionDialect.queryDefault(connection, "TNAME",
				"SELECT c.table_name AS TNAME FROM information_schema.columns c WHERE c.column_name='"
						+ PermissionGroup.permGroupIdNameOfData + "' AND table_schema='" + schemaNames[0]
						+ "'");
	}

	@Override
	protected IList<String> queryForAllPotentialPermissionGroups(Connection connection)
			throws SQLException {
		return connectionDialect.queryDefault(connection, "PERM_GROUP_NAME",
				"SELECT t.table_name AS PERM_GROUP_NAME FROM information_schema.tables t");
	}

	@Override
	public String[] createPermissionGroup(Connection connection, String tableName)
			throws SQLException {
		int maxProcedureNameLength = connection.getMetaData().getMaxProcedureNameLength();
		String permissionGroupName =
				ormPatternMatcher.buildPermissionGroupFromTableName(tableName, maxProcedureNameLength);
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
			stmt.execute(
					"DROP SCHEMA IF EXISTS \"" + connectionDialect.toDefaultCase(schemaName) + "\" CASCADE");
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
