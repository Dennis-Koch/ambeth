package de.osthus.ambeth.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.AbstractConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;

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
				functionAliases.add(rs.getString("alias_name").toUpperCase());
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
		if (functionAliases.contains(aliasName.toUpperCase()))
		{
			return;
		}
		stm.execute("CREATE ALIAS \"" + aliasName + "\" FOR \"" + functionName + "\"");
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
	public String prepareCommand(String sqlCommand)
	{
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *1 *, *0 *\\)", " BOOLEAN");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *3 *, *0 *\\)", " INT");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *5 *, *0 *\\)", " INT");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *9 *, *0 *\\)", " INT");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *10 *, *0 *\\)", " LONG");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *12 *, *0 *\\)", " LONG");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *18 *, *0 *\\)", " BIGINT");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *\\* *, *0 *\\)", " BIGINT");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER", " DOUBLE");
		sqlCommand = prepareCommandIntern(sqlCommand, "VARCHAR2 *\\( *(\\d+) *BYTE *\\)", "VARCHAR($1)");
		sqlCommand = prepareCommandIntern(sqlCommand, "VARCHAR2 *\\( *(\\d+) *CHAR *\\)", "VARCHAR($1)");
		sqlCommand = prepareCommandIntern(sqlCommand, " DEFERRABLE *INITIALLY *(?:DEFERRED|IMMEDIATE)", "");
		sqlCommand = prepareCommandIntern(sqlCommand, " NOORDER", "");
		sqlCommand = prepareCommandIntern(sqlCommand, " USING +INDEX", "");
		sqlCommand = prepareCommandIntern(sqlCommand, "MAXVALUE *9{19,} ", "MAXVALUE 999999999999999999 ");
		sqlCommand = prepareCommandIntern(sqlCommand, "DBMS_RANDOM\\.VALUE", "RAND()");
		sqlCommand = prepareCommandIntern(sqlCommand, "to_timestamp\\(", "TO_TIMESTAMP(");
		return sqlCommand;
	}

	@Override
	public String[] createOptimisticLockTrigger(Connection connection, String tableName) throws SQLException
	{
		String forTriggerName = "TR_" + tableName + "_OL";
		return new String[] { "CREATE TRIGGER " + escapeName(null, forTriggerName) + " AFTER UPDATE ON " + escapeName(null, tableName)
				+ " FOR EACH ROW CALL \"" + OptimisticLockTrigger.class.getName() + "\"" };
	}

	@Override
	public List<String> getTablesWithoutPermissionGroup(Connection conn) throws SQLException
	{
		return EmptyList.<String> getInstance();
	}

	@Override
	public String[] createPermissionGroup(Connection conn, String tableName) throws SQLException
	{
		return new String[0];
	}

	@Override
	public List<String> getTablesWithoutOptimisticLockTrigger(Connection connection) throws SQLException
	{
		Statement stm = null;
		ResultSet rs = null;
		try
		{
			stm = connection.createStatement();
			rs = stm.executeQuery("SELECT tab.table_name AS table_nam FROM INFORMATION_SCHEMA.TABLES AS tab LEFT OUTER JOIN INFORMATION_SCHEMA.TRIGGERS AS tr ON tr.table_name=tab.table_name AND tr.table_schema=tab.table_schema WHERE tab.table_type<>'SYSTEM TABLE' and (tr.java_class IS NULL OR tr.java_class<>'"
					+ OptimisticLockTrigger.class.getName() + "')");

			ArrayList<String> tableNames = new ArrayList<String>();
			// ResultSetMetaData metaData = rs.getMetaData();
			// int columnCount = metaData.getColumnCount();
			// for (int a = 0, size = columnCount; a < size; a++)
			// {
			// System.out.print(metaData.getColumnLabel(a + 1));
			// System.out.print("\t\t");
			// }
			while (rs.next())
			{
				String tableName = rs.getString("table_nam");
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
			JdbcUtil.close(stm, rs);
		}
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

		Statement stm = connection.createStatement();
		try
		{
			stm.execute("SHUTDOWN");
		}
		finally
		{
			JdbcUtil.close(stm);
		}
	}
}
