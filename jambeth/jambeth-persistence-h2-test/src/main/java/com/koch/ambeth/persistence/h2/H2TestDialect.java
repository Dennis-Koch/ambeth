package com.koch.ambeth.persistence.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.h2.Functions;
import com.koch.ambeth.persistence.h2.OptimisticLockTrigger;
import com.koch.ambeth.persistence.jdbc.AbstractConnectionTestDialect;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class H2TestDialect extends AbstractConnectionTestDialect
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void preProcessConnectionForTest(Connection connection, String[] schemaNames, boolean forcePreProcessing)
	{
		Statement stm = null;
		ResultSet rs = null;
		try
		{
			stm = connection.createStatement();
			stm.execute("SET MULTI_THREADED 1");
			stm.execute("SET DB_CLOSE_DELAY -1");
			stm.execute("CREATE SCHEMA IF NOT EXISTS \"" + schemaNames[0] + "\"");
			stm.execute("SET SCHEMA \"" + schemaNames[0] + "\"");

			rs = stm.executeQuery("SELECT alias_name FROM INFORMATION_SCHEMA.FUNCTION_ALIASES");
			HashSet<String> functionAliases = new HashSet<String>();
			while (rs.next())
			{
				functionAliases.add(rs.getString("alias_name"));
			}
			rs.close();
			createAliasIfNecessary("TO_TIMESTAMP", Functions.class.getName() + ".toTimestamp", functionAliases, stm);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(stm, rs);
		}
	}

	protected void createAliasIfNecessary(String aliasName, String functionName, Set<String> functionAliases, Statement stm) throws SQLException
	{
		if (functionAliases.contains(aliasName))
		{
			return;
		}
		stm.execute("CREATE ALIAS \"" + connectionDialect.toDefaultCase(aliasName) + "\" FOR \"" + connectionDialect.toDefaultCase(functionName) + "\"");
	}

	@Override
	public boolean isEmptySchema(Connection connection) throws SQLException
	{
		Statement stm = null;
		ResultSet rs = null;
		try
		{
			stm = connection.createStatement();
			rs = stm.executeQuery("SELECT TABLE_TYPE FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE<>'SYSTEM TABLE'");

			while (rs.next())
			{
				return false;
			}

			return true;
		}
		finally
		{
			JdbcUtil.close(stm, rs);
		}
	}

	@Override
	public String[] createOptimisticLockTrigger(Connection connection, String tableName) throws SQLException
	{
		String forTriggerName = "TR_" + tableName + "_OL";
		return new String[] { "CREATE TRIGGER " + escapeName(null, forTriggerName) + " AFTER UPDATE ON " + escapeName(null, tableName)
				+ " FOR EACH ROW CALL \"" + OptimisticLockTrigger.class.getName() + "\"" };
	}

	@Override
	public String[] createPermissionGroup(Connection conn, String tableName) throws SQLException
	{
		return new String[0];
	}

	@Override
	protected IList<String> queryForAllTables(Connection connection) throws SQLException
	{
		return connectionDialect
				.queryDefault(
						connection,
						"table_nam",
						"SELECT tab.table_name AS table_nam FROM INFORMATION_SCHEMA.TABLES AS tab LEFT OUTER JOIN INFORMATION_SCHEMA.TRIGGERS AS tr ON tr.table_name=tab.table_name AND tr.table_schema=tab.table_schema WHERE tab.table_type<>'SYSTEM TABLE' and (tr.java_class IS NULL OR tr.java_class<>'"
								+ OptimisticLockTrigger.class.getName()
								+ "') AND (NOT LOWER(tab.table_name) LIKE 'link_%') AND (NOT LOWER(tab.table_name) LIKE 'l_%')");
	}

	@Override
	protected IList<String> queryForAllPermissionGroupNeedingTables(Connection connection) throws SQLException
	{
		return EmptyList.<String> getInstance();
	}

	@Override
	protected IList<String> queryForAllPotentialPermissionGroups(Connection connection) throws SQLException
	{
		return EmptyList.<String> getInstance();
	}

	@Override
	protected IList<String> queryForAllTriggers(Connection connection) throws SQLException
	{
		return EmptyList.<String> getInstance();
	}

	@Override
	public void dropAllSchemaContent(Connection conn, String schemaName)
	{
		PreparedStatement pstm = null;
		Statement stmt2 = null;
		ResultSet rs = null;
		try
		{
			stmt2 = conn.createStatement();
			{
				pstm = conn.prepareStatement("SELECT table_schema AS schema, table_name AS name FROM INFORMATION_SCHEMA.TABLES WHERE table_schema=?");
				pstm.setString(1, schemaName);
				rs = pstm.executeQuery();
				while (rs.next())
				{
					String tableSchema = rs.getString("schema");
					String tableName = rs.getString("name");
					stmt2.execute("DROP TABLE " + escapeName(tableSchema, tableName) + " CASCADE CONSTRAINTS");
				}
				JdbcUtil.close(pstm, rs);
			}
			{
				pstm = conn.prepareStatement("SELECT table_schema AS schema, table_name AS name FROM INFORMATION_SCHEMA.VIEWS WHERE table_schema=?");
				pstm.setString(1, schemaName);
				rs = pstm.executeQuery();
				while (rs.next())
				{
					String tableSchema = rs.getString("schema");
					String tableName = rs.getString("name");
					stmt2.execute("DROP VIEW " + escapeName(tableSchema, tableName) + " CASCADE CONSTRAINTS");
				}
				JdbcUtil.close(pstm, rs);
			}
			{
				pstm = conn.prepareStatement("SELECT alias_schema AS schema, alias_name AS name FROM INFORMATION_SCHEMA.FUNCTION_ALIASES WHERE alias_schema=?");
				pstm.setString(1, schemaName);
				rs = pstm.executeQuery();
				while (rs.next())
				{
					String schema = rs.getString("schema");
					String objectName = rs.getString("name");
					stmt2.execute("DROP ALIAS " + escapeName(schema, objectName));
				}
				JdbcUtil.close(pstm, rs);
			}
			{
				pstm = conn
						.prepareStatement("SELECT sequence_schema AS schema, sequence_name AS name FROM INFORMATION_SCHEMA.SEQUENCES WHERE sequence_schema=?");
				pstm.setString(1, schemaName);
				rs = pstm.executeQuery();
				while (rs.next())
				{
					String schema = rs.getString("schema");
					String objectName = rs.getString("name");
					stmt2.execute("DROP SEQUENCE " + escapeName(schema, objectName));
				}
				JdbcUtil.close(pstm, rs);
			}
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(pstm, rs);
			JdbcUtil.close(stmt2);
		}
	}

	@Override
	public void preStructureRebuild(Connection connection) throws SQLException
	{
		super.preStructureRebuild(connection);

		// Statement stm = connection.createStatement();
		// try
		// {
		// stm.execute("SHUTDOWN");
		// }
		// finally
		// {
		// JdbcUtil.close(stm);
		// }
	}
}
