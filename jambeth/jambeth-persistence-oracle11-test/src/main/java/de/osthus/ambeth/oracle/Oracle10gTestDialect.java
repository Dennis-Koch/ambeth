package de.osthus.ambeth.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocBootstrapModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.oracle.RandomUserScript.RandomUserModule;
import de.osthus.ambeth.orm.IOrmPatternMatcher;
import de.osthus.ambeth.persistence.PermissionGroup;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.AbstractConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;

public class Oracle10gTestDialect extends AbstractConnectionTestDialect implements IInitializingBean
{
	public static final String ROOT_DATABASE_USER = "ambeth.root.database.user";

	public static final String ROOT_DATABASE_PASS = "ambeth.root.database.pass";

	protected static final String triggerNamePrefix = "TR_", triggerNamePostfix = "_OL";

	@Property(name = PersistenceConfigurationConstants.DatabaseTableIgnore, mandatory = false)
	protected String ignoredTableProperty;

	@Autowired
	protected IOrmPatternMatcher ormPatternMatcher;

	@Property(name = ROOT_DATABASE_USER, defaultValue = "sys as sysdba")
	protected String rootDatabaseUser;

	@Property(name = ROOT_DATABASE_PASS, defaultValue = "developer")
	protected String rootDatabasePass;

	protected final HashSet<String> ignoredTables = new HashSet<String>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		if (ignoredTableProperty != null)
		{
			ignoredTables.addAll(ignoredTableProperty.toUpperCase().split("[;:]"));
		}
	}

	@Override
	public boolean createTestUserIfSupported(Throwable reason, String userName, String userPassword, IProperties testProps) throws SQLException
	{
		if (!(reason instanceof SQLException))
		{
			return false;
		}
		if (((SQLException) reason).getErrorCode() != 1017) // ORA-01017: invalid username/password; logon denied
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
			bootstrapContext.createService("randomUser", RandomUserModule.class, IocBootstrapModule.class);
		}
		finally
		{
			bootstrapContext.dispose();
		}
		return true;
	}

	@Override
	public void preProcessConnectionForTest(Connection connection, String[] schemaNames, boolean forcePreProcessing)
	{
		// intended blank
	}

	@SuppressWarnings("resource")
	@Override
	public boolean isEmptySchema(Connection connection) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT tname FROM tab");
			while (rs.next())
			{
				if (!Oracle10gDialect.BIN_TABLE_NAME.matcher(rs.getString("tname")).matches()
						&& !Oracle10gDialect.IDX_TABLE_NAME.matcher(rs.getString("tname")).matches())
				{
					return false;
				}
			}
			rs.close();
			rs = stmt
					.executeQuery("SELECT object_type, object_name FROM user_objects WHERE object_type IN ('FUNCTION', 'INDEX', 'PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'SEQUENCE', 'TABLE', 'TYPE', 'VIEW')");
			return !rs.next();
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	@Override
	public String[] createOptimisticLockTrigger(Connection connection, String tableName) throws SQLException
	{
		if (Oracle10gDialect.BIN_TABLE_NAME.matcher(tableName).matches() || Oracle10gDialect.IDX_TABLE_NAME.matcher(tableName).matches())
		{
			return new String[0];
		}
		int maxNameLength = connection.getMetaData().getMaxProcedureNameLength();
		StringBuilder sb = new StringBuilder();
		String forTriggerName = tableName;
		if (forTriggerName.length() >= maxNameLength - 3 - 3) // Substract 3 chars 'TR_' and 3 chars '_OL'
		{
			forTriggerName = forTriggerName.substring(0, maxNameLength - 3 - 3);
		}
		sb.append("create or replace TRIGGER \"").append(triggerNamePrefix).append(forTriggerName).append(triggerNamePostfix);
		sb.append("\"	BEFORE UPDATE ON \"").append(tableName).append("\" FOR EACH ROW");
		sb.append(" BEGIN");
		sb.append(" if( :new.\"VERSION\" <= :old.\"VERSION\" ) then");
		sb.append(" raise_application_error( -");
		sb.append(Oracle10gDialect.getOptimisticLockErrorCode()).append(", 'Optimistic Lock Exception');");
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

			String expTriggerName = "EXPECTED_TRIGGER_NAME";
			String sqlFoundTableNamePrefix = "SELECT T.TNAME as TNAME";
			String sqlFoundTableNamePostfix = " FROM TAB T JOIN COLS C ON T.TNAME = C.TABLE_NAME WHERE T.TABTYPE='TABLE' AND C.COLUMN_NAME='VERSION'";
			String expectedTriggerNameColumn = ", concat('" + triggerNamePrefix + "', concat(T.TNAME, '" + triggerNamePostfix + "')) as " + expTriggerName;

			String sqlExpectedTriggerNames = sqlFoundTableNamePrefix + expectedTriggerNameColumn + sqlFoundTableNamePostfix;
			String sqlFoundTableNames = sqlFoundTableNamePrefix + sqlFoundTableNamePostfix;

			String foundTriggerNames = "SELECT TR.TRIGGER_NAME FROM ALL_TRIGGERS TR WHERE TR.TABLE_NAME IN (" + sqlFoundTableNames + ")";

			String sql = "SELECT TNAME FROM (" + sqlExpectedTriggerNames + ") where " + expTriggerName + " NOT IN (" + foundTriggerNames + ")";

			rs = stmt.executeQuery(sql);
			ArrayList<String> tableNames = new ArrayList<String>();
			while (rs.next())
			{
				String tableName = rs.getString("TNAME");
				if (Oracle10gDialect.BIN_TABLE_NAME.matcher(tableName).matches())
				{
					continue;
				}
				if (ignoredTables.contains(tableName))
				{
					continue;
				}
				String tableNameLower = tableName.toLowerCase();
				if (tableNameLower.startsWith("link_") || tableNameLower.startsWith("l_"))
				{
					continue;
				}
				tableNames.add(tableName);
			}
			return tableNames;
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	@Override
	public List<String> getTablesWithoutPermissionGroup(Connection connection) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();

			HashSet<String> existingPermissionGroups = new HashSet<String>();
			rs = stmt.executeQuery("SELECT TNAME FROM TAB WHERE TABTYPE='TABLE'");
			ArrayList<String> tableNames = new ArrayList<String>();
			while (rs.next())
			{
				String tableName = rs.getString("TNAME");
				if (Oracle10gDialect.BIN_TABLE_NAME.matcher(tableName).matches())
				{
					continue;
				}
				if (ignoredTables.contains(tableName))
				{
					continue;
				}
				if (ormPatternMatcher.matchesArchivePattern(tableName))
				{
					// archive tables do not need a permission group
					continue;
				}
				if (ormPatternMatcher.matchesPermissionGroupPattern(tableName))
				{
					// permissions groups themselves have no permissiong group
					existingPermissionGroups.add(tableName);
					continue;
				}
				tableNames.add(tableName);
			}
			for (int a = tableNames.size(); a-- > 0;)
			{
				String permissionGroupName = ormPatternMatcher.buildPermissionGroupFromTableName(tableNames.get(a));
				if (existingPermissionGroups.contains(permissionGroupName))
				{
					tableNames.removeAtIndex(a);
				}
			}
			return tableNames;
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	@Override
	public String[] createPermissionGroup(Connection conn, String tableName) throws SQLException
	{
		String permissionGroupName = ormPatternMatcher.buildPermissionGroupFromTableName(tableName);
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

		sql.add("CREATE TABLE " + permissionGroupName//
				+ "(" + PermissionGroup.userIdName + " VARCHAR2(64 CHAR) NOT NULL,"//
				+ PermissionGroup.permGroupIdName + " NUMBER(18,0) NOT NULL,"//
				+ PermissionGroup.readPermColumName + " NUMBER(1,0),"//
				+ "CONSTRAINT " + pkName + " PRIMARY KEY ("//
				+ PermissionGroup.userIdName + "," + PermissionGroup.permGroupIdName//
				+ ") USING INDEX )");

		sql.add("CREATE INDEX " + permissionGroupName + "_IDX" + " ON " + permissionGroupName + " (" + PermissionGroup.permGroupIdName + ")");

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
	public String prepareCommand(String sqlCommand)
	{
		return sqlCommand;
	}

	@Override
	public void dropAllSchemaContent(Connection conn, String schemaName)
	{
		Statement stmt = null, stmt2 = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement();
			stmt2 = conn.createStatement();
			stmt.execute("SELECT TNAME, TABTYPE FROM TAB");
			rs = stmt.getResultSet();
			while (rs.next())
			{
				String tableName = rs.getString(1);
				if (Oracle10gDialect.BIN_TABLE_NAME.matcher(tableName).matches() || Oracle10gDialect.IDX_TABLE_NAME.matcher(tableName).matches())
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
				if (Oracle10gDialect.BIN_TABLE_NAME.matcher(objectName).matches() || Oracle10gDialect.IDX_TABLE_NAME.matcher(objectName).matches())
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
