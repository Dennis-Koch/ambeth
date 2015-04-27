package de.osthus.ambeth.pg;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.orm.IOrmPatternMatcher;
import de.osthus.ambeth.persistence.PermissionGroup;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.AbstractConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.pg.RandomUserScript.RandomUserModule;
import de.osthus.ambeth.sql.ISqlBuilder;

public class PostgresTestDialect extends AbstractConnectionTestDialect implements IInitializingBean
{
	public static final String ROOT_DATABASE_USER = "ambeth.root.database.user";

	public static final String ROOT_DATABASE_PASS = "ambeth.root.database.pass";

	@Autowired
	protected IOrmPatternMatcher ormPatternMatcher;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Property(name = PersistenceConfigurationConstants.DatabaseTableIgnore, mandatory = false)
	protected String ignoredTableProperty;

	@Property(name = ROOT_DATABASE_USER, defaultValue = "postgres")
	protected String rootDatabaseUser;

	@Property(name = ROOT_DATABASE_PASS, defaultValue = "developer")
	protected String rootDatabasePass;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName)
	protected String schemaName;

	protected String[] schemaNames;

	protected final HashSet<String> ignoredTables = new HashSet<String>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		if (ignoredTableProperty != null)
		{
			ignoredTables.addAll(connectionDialect.toDefaultCase(ignoredTableProperty).split("[;:]"));
		}
		schemaNames = connectionDialect.toDefaultCase(schemaName).split("[:;]");
	}

	@Override
	public boolean createTestUserIfSupported(Throwable reason, String userName, String userPassword, IProperties testProps) throws SQLException
	{
		if (!(reason instanceof SQLException))
		{
			return false;
		}
		if (!"28P01".equals(((SQLException) reason).getSQLState()) // INVALID PASSWORD, FATAL: password authentication failed for user "xxx"
				&& !"3D000".equals(((SQLException) reason).getSQLState()) // FATAL: database "xxx" does not exist
		)
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
	public void preStructureRebuild(Connection connection) throws SQLException
	{
		super.preStructureRebuild(connection);

		Statement stm = null;
		try
		{
			stm = connection.createStatement();
			for (String schemaName : schemaNames)
			{
				stm.execute("CREATE SCHEMA IF NOT EXISTS \"" + schemaName + "\"");
			}
			stm.execute("SET SCHEMA '" + schemaNames[0] + "'");
		}
		finally
		{
			JdbcUtil.close(stm);
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

			rs = stmt
					.executeQuery("SELECT count(*) FROM pg_class c INNER JOIN pg_namespace n ON c.relnamespace=n.oid WHERE n.nspname='" + schemaNames[0] + "'");
			rs.next();
			return rs.getInt(1) == 0;
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	@Override
	public String[] createOptimisticLockTrigger(Connection connection, String fullyQualifiedTableName) throws SQLException
	{
		if (PostgresDialect.BIN_TABLE_NAME.matcher(fullyQualifiedTableName).matches()
				|| PostgresDialect.IDX_TABLE_NAME.matcher(fullyQualifiedTableName).matches())
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
		String triggerName = ormPatternMatcher.buildOptimisticLockTriggerFromTableName(fullyQualifiedTableName, maxProcedureNameLength);

		String functionName = "f_" + triggerName;
		String[] sql = new String[2];
		{
			AppendableStringBuilder sb = new AppendableStringBuilder();
			sb.append("CREATE OR REPLACE FUNCTION ").append(functionName).append("() RETURNS TRIGGER AS $").append(functionName).append("$\n");
			sb.append(" BEGIN\n");
			sb.append("  IF NEW.\"").append("VERSION").append("\" <= OLD.\"").append("VERSION").append("\" THEN\n");
			sb.append("  RAISE EXCEPTION '").append(Integer.toString(PostgresDialect.getOptimisticLockErrorCode())).append(" Optimistic Lock Exception';\n");
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
			if (tableColumns.size() > 0)
			{
				sb.append(" OF ");
				for (int a = 0, size = tableColumns.size(); a < size; a++)
				{
					if (a > 0)
					{
						sb.append(',');
					}
					sqlBuilder.escapeName(tableColumns.get(a), sb);
				}
			}
			sb.append(" ON \"").append(names[1]).append("\" FOR EACH ROW EXECUTE PROCEDURE ").append(functionName).append("()");
			sql[1] = sb.toString();
		}
		return sql;
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
					.executeQuery("SELECT DISTINCT n.nspname || '.' || c.relname AS FULL_NAME FROM pg_trigger t JOIN pg_class c ON t.tgrelid=c.oid JOIN pg_namespace n ON c.relnamespace=n.oid WHERE n.nspname='"
							+ schemaNames[0] + "'");
			ArrayList<String> tableNamesWhichNeedOptimisticLockTrigger = new ArrayList<String>();
			while (rs.next())
			{
				String tableName = rs.getString("FULL_NAME");
				if (PostgresDialect.BIN_TABLE_NAME.matcher(tableName).matches())
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
			rs = stmt.executeQuery("SELECT t.tgname AS TRIGGER_NAME FROM pg_trigger t");
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
	public List<String> getTablesWithoutPermissionGroup(Connection connection) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();

			HashSet<String> existingPermissionGroups = new HashSet<String>();

			rs = stmt.executeQuery("SELECT c.table_name AS TNAME FROM information_schema.columns c WHERE c.column_name='"
					+ PermissionGroup.permGroupIdNameOfData + "' AND table_schema='" + schemaNames[0] + "'");

			ArrayList<String> tableNamesWhichNeedPermissionGroup = new ArrayList<String>();
			while (rs.next())
			{
				String tableName = rs.getString("TNAME");
				if (PostgresDialect.BIN_TABLE_NAME.matcher(tableName).matches())
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
				tableNamesWhichNeedPermissionGroup.add(tableName);
			}
			JdbcUtil.close(rs);
			rs = stmt.executeQuery("SELECT t.table_name AS TNAME FROM information_schema.tables t");
			while (rs.next())
			{
				String tableName = rs.getString("TNAME");
				if (ormPatternMatcher.matchesPermissionGroupPattern(tableName))
				{
					existingPermissionGroups.add(tableName);
				}
			}
			int maxProcedureNameLength = connection.getMetaData().getMaxProcedureNameLength();
			for (int a = tableNamesWhichNeedPermissionGroup.size(); a-- > 0;)
			{
				String permissionGroupName = ormPatternMatcher.buildPermissionGroupFromTableName(tableNamesWhichNeedPermissionGroup.get(a),
						maxProcedureNameLength);
				if (existingPermissionGroups.contains(permissionGroupName))
				{
					tableNamesWhichNeedPermissionGroup.removeAtIndex(a);
				}
			}
			return tableNamesWhichNeedPermissionGroup;
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
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
	public String prepareCommand(String sqlCommand)
	{
		Pattern pattern = Pattern.compile(" *create or replace TYPE ([^ ]+) AS VARRAY\\(\\d+\\) OF +(.+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sqlCommand);
		if (matcher.matches())
		{
			String arrayTypeName = matcher.group(1);
			if (arrayTypeName.equalsIgnoreCase("STRING_ARRAY"))
			{
				return "";
			}
		}

		sqlCommand = prepareCommandIntern(sqlCommand, " BLOB", " BYTEA");
		sqlCommand = prepareCommandIntern(sqlCommand, " CLOB", " TEXT");

		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *1 *, *0 *\\)", " BOOLEAN");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *[0-9] *, *0 *\\)", " INTEGER");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *1[0,1,2,3,4,5,6,7,8] *, *0 *\\)", " BIGINT");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *\\d+ *\\, *\\d+ *\\)", " NUMERIC");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *\\d+ *\\)", " NUMERIC");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER[^\"]", " NUMERIC");
		// sqlCommand = prepareCommandIntern(sqlCommand, "(?: |\")NUMBER *\\(", " NUMERIC\\(");

		sqlCommand = prepareCommandInternWithGroup(sqlCommand, " VARCHAR *\\( *(\\d+) +CHAR *\\)", " TEXT");

		sqlCommand = prepareCommandInternWithGroup(sqlCommand, " VARCHAR2 *\\( *(\\d+) +BYTE\\)", " VARCHAR(\\2)");
		sqlCommand = prepareCommandInternWithGroup(sqlCommand, " VARCHAR2 *\\( *(\\d+) +CHAR\\)", " VARCHAR(\\2)");

		sqlCommand = prepareCommandInternWithGroup(sqlCommand, " PRIMARY KEY (\\([^\\)]+\\)) USING INDEX", " PRIMARY KEY \\2");
		sqlCommand = prepareCommandInternWithGroup(sqlCommand, " PRIMARY KEY (\\([^\\)]+\\)) USING INDEX", " PRIMARY KEY \\2");

		sqlCommand = prepareCommandInternWithGroup(sqlCommand, "([^a-zA-Z0-9])STRING_ARRAY([^a-zA-Z0-9])", "\\2TEXT[]\\3");

		sqlCommand = prepareCommandIntern(sqlCommand, " NOORDER", "");
		sqlCommand = prepareCommandIntern(sqlCommand, " NOCYCLE", "");
		sqlCommand = prepareCommandIntern(sqlCommand, " USING +INDEX", "");

		sqlCommand = prepareCommandIntern(sqlCommand, " 999999999999999999999999999 ", " 9223372036854775807 ");

		return sqlCommand;
	}

	@Override
	public void dropAllSchemaContent(Connection connection, String schemaName)
	{
		Statement stmt = null, stmt2 = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DROP SCHEMA IF EXISTS \"" + connectionDialect.toDefaultCase(schemaName) + "\" CASCADE");
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
