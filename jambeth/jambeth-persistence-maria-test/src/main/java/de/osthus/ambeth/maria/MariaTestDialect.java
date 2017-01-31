package de.osthus.ambeth.maria;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.maria.RandomUserScript.RandomUserModule;
import de.osthus.ambeth.persistence.PermissionGroup;
import de.osthus.ambeth.persistence.jdbc.AbstractConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.sql.ISqlBuilder;

public class MariaTestDialect extends AbstractConnectionTestDialect
{
	public static final String ROOT_DATABASE_USER = "ambeth.root.database.user";

	public static final String ROOT_DATABASE_PASS = "ambeth.root.database.pass";

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Property(name = ROOT_DATABASE_USER, defaultValue = "root")
	protected String rootDatabaseUser;

	@Property(name = ROOT_DATABASE_PASS, defaultValue = "")
	protected String rootDatabasePass;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName)
	protected String schemaName;

	protected String[] schemaNames;

	protected final HashSet<String> ignoredTables = new HashSet<String>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
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
				&& !"28000".equals(((SQLException) reason).getSQLState()) // INVALID AUTHORIZATION SPECIFICATION undefined role tried to access the server
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
				try
				{
					stm.execute("CREATE SCHEMA " + connectionDialect.escapeName(schemaName));
				}
				catch (Throwable e)
				{
					// intended blank
				}
			}
			stm.execute("USE " + connectionDialect.escapeName(schemaNames[0]));
			stm.execute("CREATE TABLE " + connectionDialect.escapeName(MariaDialect.SEQUENCE_TABLE_NAME) + " ("
					+ connectionDialect.escapeName(MariaDialect.SEQUENCE_COLUMN_NAME) + " varchar(100) NOT NULL,"//
					+ connectionDialect.escapeName(MariaDialect.SEQUENCE_INCREMENT_NAME) + " int(11) unsigned NOT NULL DEFAULT 1," //
					+ connectionDialect.escapeName(MariaDialect.SEQUENCE_MIN_NAME) + " int(11) unsigned NOT NULL DEFAULT 1," //
					+ connectionDialect.escapeName(MariaDialect.SEQUENCE_MAX_NAME) + " bigint(20) unsigned NOT NULL DEFAULT 18446744073709551615," //
					+ connectionDialect.escapeName(MariaDialect.SEQUENCE_CUR_NAME) + " bigint(20) unsigned DEFAULT 1," //
					+ connectionDialect.escapeName(MariaDialect.SEQUENCE_CYCLE_NAME) + " boolean NOT NULL DEFAULT FALSE," //
					+ " PRIMARY KEY (" + connectionDialect.escapeName(MariaDialect.SEQUENCE_COLUMN_NAME) + "))");

			stm.execute("CREATE FUNCTION "
					+ connectionDialect.escapeName(MariaDialect.NEXT_VAL_FUNCTION_NAME)
					+ " (`seq_name` varchar(100))\n"//
					+ "RETURNS bigint(20) NOT DETERMINISTIC\n"//
					+ "BEGIN\n"//
					+ " DECLARE cur_val bigint(20);\n"//
					+ " SELECT\n"//
					+ "  "
					+ connectionDialect.escapeName(MariaDialect.SEQUENCE_CUR_NAME)
					+ " INTO cur_val\n"//
					+ " FROM\n"//
					+ "  "
					+ connectionDialect.escapeName(MariaDialect.SEQUENCE_TABLE_NAME)
					+ "\n"//
					+ " WHERE\n"//
					+ "  "
					+ connectionDialect.escapeName(MariaDialect.SEQUENCE_COLUMN_NAME)
					+ " = seq_name\n"//
					+ " ;\n"//
					+ " IF cur_val IS NOT NULL THEN\n"//
					+ "  UPDATE\n"//
					+ "   "
					+ connectionDialect.escapeName(MariaDialect.SEQUENCE_TABLE_NAME)
					+ "\n"//
					+ "  SET\n"//
					+ "   "
					+ connectionDialect.escapeName(MariaDialect.SEQUENCE_CUR_NAME)
					+ " = IF (\n"//
					+ "    (" + connectionDialect.escapeName(MariaDialect.SEQUENCE_CUR_NAME) + " + "
					+ connectionDialect.escapeName(MariaDialect.SEQUENCE_INCREMENT_NAME) + ") > "
					+ connectionDialect.escapeName(MariaDialect.SEQUENCE_MAX_NAME)
					+ ",\n"//
					+ "    IF (\n"//
					+ "     " + connectionDialect.escapeName(MariaDialect.SEQUENCE_CYCLE_NAME)
					+ " = TRUE,\n"//
					+ "     " + connectionDialect.escapeName(MariaDialect.SEQUENCE_MIN_NAME)
					+ ",\n"//
					+ "     NULL\n"//
					+ "    ),\n"//
					+ "    " + connectionDialect.escapeName(MariaDialect.SEQUENCE_CUR_NAME) + " + "
					+ connectionDialect.escapeName(MariaDialect.SEQUENCE_INCREMENT_NAME) + "\n"//
					+ "   )\n"//
					+ "  WHERE\n"//
					+ "    " + connectionDialect.escapeName(MariaDialect.SEQUENCE_COLUMN_NAME) + " = seq_name\n"//
					+ "   ;\n"//
					+ " END IF;\n"//
					+ " RETURN cur_val;\n"//
					+ "END");
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

			String dbName;
			try
			{
				rs = stmt.executeQuery("SELECT DATABASE()");
				rs.next();
				dbName = rs.getString(1);
			}
			finally
			{
				JdbcUtil.close(rs);
			}

			rs = stmt.executeQuery("SELECT COUNT(DISTINCT 'table_name') FROM `information_schema`.`columns` WHERE `table_schema` = '" + dbName + "'");
			rs.next();
			return rs.getInt(1) == 0;
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	@Override
	public String[] createOptimisticLockTrigger(Connection connection, String fqTableName) throws SQLException
	{
		if (MariaDialect.BIN_TABLE_NAME.matcher(fqTableName).matches() || MariaDialect.IDX_TABLE_NAME.matcher(fqTableName).matches())
		{
			return new String[0];
		}
		String[] names = sqlBuilder.getSchemaAndTableName(fqTableName);
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
		String triggerName = ormPatternMatcher.buildOptimisticLockTriggerFromTableName(fqTableName, maxProcedureNameLength);

		String functionName = "f_" + triggerName;
		String[] sql = new String[2];
		{
			AppendableStringBuilder sb = new AppendableStringBuilder();
			sb.append("CREATE OR REPLACE FUNCTION ").append(functionName).append("() RETURNS TRIGGER AS $").append(functionName).append("$\n");
			sb.append(" BEGIN\n");
			sb.append("  IF NEW.\"").append("VERSION").append("\" <= OLD.\"").append("VERSION").append("\" THEN\n");
			sb.append("  RAISE EXCEPTION '").append(Integer.toString(MariaDialect.getOptimisticLockErrorCode())).append(" Optimistic Lock Exception';\n");
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
					connectionDialect.escapeName(tableColumns.get(a), sb);
				}
			}
			sb.append(" ON \"").append(names[1]).append("\" FOR EACH ROW EXECUTE PROCEDURE ").append(functionName).append("()");
			sql[1] = sb.toString();
		}
		return sql;
	}

	@Override
	protected boolean isTableNameToIgnore(String tableName)
	{
		if (MariaDialect.BIN_TABLE_NAME.matcher(tableName).matches())
		{
			return true;
		}
		return false;
	}

	@Override
	protected IList<String> queryForAllTables(Connection connection) throws SQLException
	{
		return new ArrayList<String>();
	}

	@Override
	protected IList<String> queryForAllTriggers(Connection connection) throws SQLException
	{
		return connectionDialect.queryDefault(connection, "TRIGGER_NAME", "SELECT t.trigger_name AS TRIGGER_NAME FROM information_schema.triggers t");
	}

	@Override
	protected IList<String> queryForAllPermissionGroupNeedingTables(Connection connection) throws SQLException
	{
		return connectionDialect.queryDefault(connection, "TNAME", "SELECT c.table_name AS TNAME FROM information_schema.columns c WHERE c.column_name='"
				+ PermissionGroup.permGroupIdNameOfData + "' AND table_schema='" + schemaNames[0] + "'");
	}

	@Override
	protected IList<String> queryForAllPotentialPermissionGroups(Connection connection) throws SQLException
	{
		return connectionDialect.queryDefault(connection, "PERM_GROUP_NAME", "SELECT t.table_name AS PERM_GROUP_NAME FROM information_schema.tables t");
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
			stmt.execute("DROP SCHEMA " + connectionDialect.toDefaultCase(schemaName));
			stmt.execute("CREATE SCHEMA " + connectionDialect.toDefaultCase(schemaName));
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
