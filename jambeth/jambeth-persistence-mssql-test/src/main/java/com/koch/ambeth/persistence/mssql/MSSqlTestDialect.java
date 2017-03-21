package com.koch.ambeth.persistence.mssql;

/*-
 * #%L
 * jambeth-persistence-mssql-test
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.PermissionGroup;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.AbstractConnectionTestDialect;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.mssql.MSSqlDialect;
import com.koch.ambeth.persistence.mssql.RandomUserScript.RandomUserModule;
import com.koch.ambeth.persistence.orm.IOrmPatternMatcher;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class MSSqlTestDialect extends AbstractConnectionTestDialect implements IInitializingBean
{
	public static final String ROOT_DATABASE_USER = "ambeth.root.database.user";

	public static final String ROOT_DATABASE_PASS = "ambeth.root.database.pass";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IOrmPatternMatcher ormPatternMatcher;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Property(name = PersistenceConfigurationConstants.DatabaseTableIgnore, mandatory = false)
	protected String ignoredTableProperty;

	@Property(name = ROOT_DATABASE_USER, defaultValue = "sa")
	protected String rootDatabaseUser;

	@Property(name = ROOT_DATABASE_PASS, defaultValue = "developer")
	protected String rootDatabasePass;

	protected final HashSet<String> ignoredTables = new HashSet<String>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		if (ignoredTableProperty != null)
		{
			ignoredTables.addAll(connectionDialect.toDefaultCase(ignoredTableProperty).split("[;:]"));
		}
	}

	@Override
	public boolean createTestUserIfSupported(Throwable reason, String userName, String userPassword, IProperties testProps) throws SQLException
	{
		if (!(reason instanceof SQLException))
		{
			return false;
		}
		if (((SQLException) reason).getErrorCode() != 18456) // Login failed for user
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
		IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(createUserProps);
		try
		{
			bootstrapContext.createService("randomUser", RandomUserModule.class, IocModule.class);
		}
		finally
		{
			bootstrapContext.dispose();
		}
		return true;
	}

	@Override
	public void dropCreatedTestUser(String userName, String userPassword, IProperties testProps) throws SQLException
	{
		Properties createUserProps = new Properties(testProps);
		createUserProps.put(RandomUserScript.SCRIPT_IS_CREATE, "false");
		createUserProps.put(RandomUserScript.SCRIPT_USER_NAME, userName);
		createUserProps.put(RandomUserScript.SCRIPT_USER_PASS, userPassword);

		createUserProps.put(PersistenceJdbcConfigurationConstants.DatabaseUser, rootDatabaseUser);
		createUserProps.put(PersistenceJdbcConfigurationConstants.DatabasePass, rootDatabasePass);
		IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(createUserProps);
		try
		{
			bootstrapContext.createService("randomUser", RandomUserModule.class, IocModule.class);
		}
		finally
		{
			bootstrapContext.dispose();
		}
	}

	@Override
	public void preProcessConnectionForTest(Connection connection, String[] schemaNames, boolean forcePreProcessing)
	{
		// intended blank
	}

	@Override
	public boolean isEmptySchema(Connection connection) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT * FROM sys.all_objects WHERE is_ms_shipped<>1");
			return !rs.next();
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	@Override
	public String[] createOptimisticLockTrigger(Connection connection, String fullyQualifiedTableName) throws SQLException
	{
		if (MSSqlDialect.BIN_TABLE_NAME.matcher(fullyQualifiedTableName).matches() || MSSqlDialect.IDX_TABLE_NAME.matcher(fullyQualifiedTableName).matches())
		{
			return new String[0];
		}
		String[] names = sqlBuilder.getSchemaAndTableName(fullyQualifiedTableName);
		ArrayList<String> tableColumns = new ArrayList<String>();
		ResultSet tableColumnsRS = connection.getMetaData().getColumns(null, names[0], names[1], null);
		try
		{
			while (tableColumnsRS.next())
			{
				String columnName = tableColumnsRS.getString("COLUMN_NAME");
				if (columnName.equalsIgnoreCase(PermissionGroup.permGroupIdNameOfData))
				{
					continue;
				}
				int columnType = tableColumnsRS.getInt("DATA_TYPE");
				if (java.sql.Types.CLOB == columnType || java.sql.Types.BLOB == columnType)
				{
					// ORA-25006: cannot specify this column in UPDATE OF clause
					// lobs have a lob locator as a pointer to the internal technical lob storage. the lob locator is never changed when a lob is initialized or
					// updated
					continue;
				}
				tableColumns.add(columnName);
			}
		}
		finally
		{
			JdbcUtil.close(tableColumnsRS);
		}
		int maxProcedureNameLength = connection.getMetaData().getMaxProcedureNameLength();
		AppendableStringBuilder sb = new AppendableStringBuilder();
		String triggerName = ormPatternMatcher.buildOptimisticLockTriggerFromTableName(fullyQualifiedTableName, maxProcedureNameLength);
		sb.append("create or replace TRIGGER \"").append(triggerName);
		sb.append("\" BEFORE UPDATE");
		if (tableColumns.size() > 0)
		{
			sb.append(" OF ");
			for (int a = 0, size = tableColumns.size(); a < size; a++)
			{
				if (a > 0)
				{
					sb.append(',');
				}
				connectionDialect.escapeName(tableColumns.get(a), sb);
			}
		}
		sb.append(" ON \"").append(names[1]).append("\" FOR EACH ROW");
		sb.append(" BEGIN");
		sb.append(" if( :new.\"VERSION\" <= :old.\"VERSION\" ) then");
		sb.append(" raise_application_error( -");
		sb.append(Integer.toString(MSSqlDialect.getOptimisticLockErrorCode())).append(", 'Optimistic Lock Exception');");
		sb.append(" end if;");
		sb.append(" END;");
		return new String[] { sb.toString() };
	}

	@Override
	public List<String> getTablesWithoutOptimisticLockTrigger(Connection connection) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();

			HashSet<String> existingOptimisticLockTriggers = new HashSet<String>();
			rs = stmt
					.executeQuery("SELECT USER || '.' || TNAME FULL_NAME FROM DUAL, TAB T JOIN COLS C ON T.TNAME = C.TABLE_NAME WHERE T.TABTYPE='TABLE' AND C.COLUMN_NAME='VERSION'");
			ArrayList<String> tableNamesWhichNeedOptimisticLockTrigger = new ArrayList<String>();
			while (rs.next())
			{
				String tableName = rs.getString("FULL_NAME");
				if (MSSqlDialect.BIN_TABLE_NAME.matcher(tableName).matches())
				{
					continue;
				}
				if (ignoredTables.contains(tableName))
				{
					continue;
				}
				if (ormPatternMatcher.matchesArchivePattern(tableName))
				{
					// archive tables do not need an optimistic lock trigger
					continue;
				}
				tableNamesWhichNeedOptimisticLockTrigger.add(tableName);
			}
			JdbcUtil.close(rs);
			rs = stmt.executeQuery("SELECT TRIGGER_NAME FROM ALL_TRIGGERS");
			while (rs.next())
			{
				String triggerName = rs.getString("TRIGGER_NAME");
				if (ormPatternMatcher.matchesOptimisticLockTriggerPattern(triggerName))
				{
					existingOptimisticLockTriggers.add(triggerName);
				}
			}
			int maxProcedureNameLength = connection.getMetaData().getMaxProcedureNameLength();
			for (int a = tableNamesWhichNeedOptimisticLockTrigger.size(); a-- > 0;)
			{
				String permissionGroupName = ormPatternMatcher.buildPermissionGroupFromTableName(tableNamesWhichNeedOptimisticLockTrigger.get(a),
						maxProcedureNameLength);
				if (existingOptimisticLockTriggers.contains(permissionGroupName))
				{
					tableNamesWhichNeedOptimisticLockTrigger.removeAtIndex(a);
				}
			}
			return tableNamesWhichNeedOptimisticLockTrigger;
			// HashSet<String> existingPermissionGroups = new HashSet<String>();
			//
			// String expTriggerName = "EXPECTED_TRIGGER_NAME";
			// String sqlFoundTableNamePrefix = "SELECT T.TNAME as TNAME";
			// String sqlFoundTableNamePostfix = " FROM TAB T JOIN COLS C ON T.TNAME = C.TABLE_NAME WHERE T.TABTYPE='TABLE' AND C.COLUMN_NAME='VERSION'";
			// String expectedTriggerNameColumn = ", concat('" + triggerNamePrefix + "', concat(T.TNAME, '" + triggerNamePostfix + "')) as " + expTriggerName;
			//
			// String sqlExpectedTriggerNames = sqlFoundTableNamePrefix + expectedTriggerNameColumn + sqlFoundTableNamePostfix;
			// String sqlFoundTableNames = sqlFoundTableNamePrefix + sqlFoundTableNamePostfix;
			//
			// String foundTriggerNames = "SELECT TR.TRIGGER_NAME FROM ALL_TRIGGERS TR WHERE TR.TABLE_NAME IN (" + sqlFoundTableNames + ")";
			//
			// String sql = "SELECT TNAME FROM (" + sqlExpectedTriggerNames + ") where " + expTriggerName + " NOT IN (" + foundTriggerNames + ")";
			//
			// rs = stmt.executeQuery(sql);
			// ArrayList<String> tableNames = new ArrayList<String>();
			// while (rs.next())
			// {
			// String tableName = rs.getString("TNAME");
			// if (Oracle10gDialect.BIN_TABLE_NAME.matcher(tableName).matches())
			// {
			// continue;
			// }
			// if (ignoredTables.contains(tableName))
			// {
			// continue;
			// }
			// String tableNameLower = tableName.toLowerCase();
			// if (tableNameLower.startsWith("link_") || tableNameLower.startsWith("l_"))
			// {
			// continue;
			// }
			// tableNames.add(tableName);
			// }
			// return tableNames;
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	@Override
	protected IList<String> queryForAllPermissionGroupNeedingTables(Connection connection) throws SQLException
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	protected IList<String> queryForAllPotentialPermissionGroups(Connection connection) throws SQLException
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	protected IList<String> queryForAllTables(Connection connection) throws SQLException
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	protected com.koch.ambeth.util.collections.IList<String> queryForAllTriggers(Connection connection) throws SQLException
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public String[] createPermissionGroup(Connection connection, String tableName) throws SQLException
	{
		int maxProcedureNameLength = connection.getMetaData().getMaxProcedureNameLength();
		String permissionGroupName = ormPatternMatcher.buildPermissionGroupFromTableName(tableName, maxProcedureNameLength);
		String pkName;
		Matcher matcher = Pattern.compile("(?:.*\\.)?([^\\.]+)").matcher(permissionGroupName);
		if (matcher.matches())
		{
			pkName = matcher.group(1) + "_PK";
		}
		else
		{
			pkName = tableName + "_PK";
		}
		ArrayList<String> sql = new ArrayList<String>();

		sql.add("CREATE TABLE \"" + permissionGroupName + "\" "//
				+ "(\"" + PermissionGroup.userIdName + "\" VARCHAR2(64 CHAR) NOT NULL,"//
				+ "\"" + PermissionGroup.permGroupIdName + "\" NUMBER(18,0) NOT NULL,"//
				+ "\"" + PermissionGroup.readPermColumName + "\" NUMBER(1,0),"//
				+ "\"" + PermissionGroup.updatePermColumName + "\" NUMBER(1,0),"//
				+ "\"" + PermissionGroup.deletePermColumName + "\" NUMBER(1,0),"//
				+ "CONSTRAINT \"" + pkName + "\" PRIMARY KEY ("//
				+ "\"" + PermissionGroup.userIdName + "\",\"" + PermissionGroup.permGroupIdName + "\""//
				+ ") USING INDEX )");

		sql.add("CREATE INDEX \"" + permissionGroupName + "_IDX\"" + " ON \"" + permissionGroupName + "\" (\"" + PermissionGroup.permGroupIdName + "\")");

		// PreparedStatement pstm = null;
		// ResultSet rs = null;
		// try
		// {
		// pstm = conn
		// .prepareStatement("SELECT T.TNAME as TNAME FROM TAB T LEFT OUTER JOIN COLS C ON T.TNAME = C.TABLE_NAME WHERE C.COLUMN_NAME=? AND T.TNAME IN (?)");
		// pstm.setString(1, PermissionGroup.permGroupIdNameOfData);
		// pstm.setString(2, tableName);
		// rs = pstm.executeQuery();
		// if (!rs.next())
		// {
		// sql.add("ALTER TABLE " + tableName + " ADD \"" + PermissionGroup.permGroupIdNameOfData + "\" NUMBER(18,0)");
		// }
		// }
		// finally
		// {
		// JdbcUtil.close(pstm, rs);
		// }

		return sql.toArray(String.class);
	}

	@Override
	public void dropAllSchemaContent(Connection connection, String schemaName)
	{
		Statement stmt = null, stmt2 = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			stmt2 = connection.createStatement();
			stmt.execute("SELECT TNAME, TABTYPE FROM TAB");
			rs = stmt.getResultSet();
			while (rs.next())
			{
				String tableName = rs.getString(1);
				if (MSSqlDialect.BIN_TABLE_NAME.matcher(tableName).matches() || MSSqlDialect.IDX_TABLE_NAME.matcher(tableName).matches())
				{
					continue;
				}
				String tableType = rs.getString(2);
				if ("VIEW".equalsIgnoreCase(tableType))
				{
					stmt2.execute("DROP VIEW " + escapeName(schemaName, tableName) + " CASCADE CONSTRAINTS");
				}
				else if ("TABLE".equalsIgnoreCase(tableType))
				{
					stmt2.execute("DROP TABLE " + escapeName(schemaName, tableName) + " CASCADE CONSTRAINTS");
				}
				else if ("SYNONYM".equalsIgnoreCase(tableType))
				{
					stmt2.execute("DROP SYNONYM " + escapeName(schemaName, tableName));
				}
			}
			JdbcUtil.close(rs);
			rs = stmt
					.executeQuery("SELECT object_type, object_name FROM user_objects WHERE object_type IN ('FUNCTION', 'INDEX', 'PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'SEQUENCE', 'SYNONYM', 'TABLE', 'TYPE', 'VIEW')");
			while (rs.next())
			{
				String objectType = rs.getString("object_type");
				String objectName = rs.getString("object_name");
				if (MSSqlDialect.BIN_TABLE_NAME.matcher(objectName).matches() || MSSqlDialect.IDX_TABLE_NAME.matcher(objectName).matches())
				{
					continue;
				}
				stmt2.execute("DROP " + objectType + " " + escapeName(schemaName, objectName));
			}
			stmt2.execute("PURGE RECYCLEBIN");
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
			JdbcUtil.close(stmt2);
		}
	}
}
