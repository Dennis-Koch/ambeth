package de.osthus.ambeth.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.EmptyMap;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.AbstractConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;

public class H2Dialect extends AbstractConnectionDialect implements IInitializingBean
{
	public static int getOptimisticLockErrorCodeStatic()
	{
		return 10001;
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
	}

	@Override
	public void preProcessConnection(Connection connection, String[] schemaNames, boolean forcePreProcessing)
	{
		Statement stm = null;
		ResultSet rs = null;
		try
		{
			stm = connection.createStatement();

			rs = stm.executeQuery("SELECT alias_name FROM INFORMATION_SCHEMA.FUNCTION_ALIASES");
			HashSet<String> functionAliases = new HashSet<String>();
			while (rs.next())
			{
				functionAliases.add(rs.getString("alias_name").toUpperCase());
			}
			rs.close();
			rs = stm.executeQuery("SELECT * FROM INFORMATION_SCHEMA.FUNCTION_ALIASES");
			printResultSet(rs);
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
	public IList<IMap<String, String>> getExportedKeys(Connection connection, String schemaName) throws SQLException
	{
		Statement stm = null;
		ResultSet rs = null;
		try
		{
			ArrayList<IMap<String, String>> allForeignKeys = new ArrayList<IMap<String, String>>();
			// rs = connection.getMetaData().getExportedKeys(null, null, null);
			stm = connection.createStatement();
			// rs = stm.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS WHERE constraint_type='REFERENTIAL'");
			rs = stm.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CROSS_REFERENCES");
			// printResultSet(rs);
			while (rs.next())
			{
				HashMap<String, String> foreignKey = new HashMap<String, String>();

				foreignKey.put("OWNER", schemaName);
				foreignKey.put("CONSTRAINT_NAME", rs.getString("FK_NAME").toUpperCase());
				foreignKey.put("FKTABLE_NAME", rs.getString("FKTABLE_NAME").toUpperCase());
				foreignKey.put("FKCOLUMN_NAME", rs.getString("FKCOLUMN_NAME").toUpperCase());
				foreignKey.put("PKTABLE_NAME", rs.getString("PKTABLE_NAME").toUpperCase());
				foreignKey.put("PKCOLUMN_NAME", rs.getString("PKCOLUMN_NAME").toUpperCase());

				allForeignKeys.add(foreignKey);
			}
			return allForeignKeys;
		}
		finally
		{
			JdbcUtil.close(stm, rs);
		}
	}

	@Override
	public ILinkedMap<String, IList<String>> getFulltextIndexes(Connection connection, String schemaName) throws SQLException
	{
		return EmptyMap.emptyMap();
	}

	@Override
	public boolean isSystemTable(String tableName)
	{
		return false;
	}

	@Override
	public boolean handleField(Class<?> fieldType, Object value, StringBuilder targetSb) throws Throwable
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IList<String[]> disableConstraints(Connection connection)
	{
		// H2 does not support deferrable constraints
		return EmptyList.getInstance();
	}

	@Override
	public void enableConstraints(Connection connection, IList<String[]> disabled)
	{
		// H2 does not support deferrable constraints
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint, Connection connection) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int getOptimisticLockErrorCode()
	{
		return getOptimisticLockErrorCodeStatic();
	}

	@Override
	public int getResourceBusyErrorCode()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public PersistenceException createPersistenceException(SQLException e, String relatedSql)
	{
		int errorCode = e.getErrorCode();
		if (errorCode == getOptimisticLockErrorCode())
		{
			OptimisticLockException ex = new OptimisticLockException(relatedSql, e);
			ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
			return ex;
		}
		return null;
	}

	@Override
	public ResultSet getIndexInfo(Connection connection, String schemaName, String tableName, boolean unique) throws SQLException
	{
		return connection.getMetaData().getIndexInfo(null, schemaName, tableName, unique, true);
	}

	@Override
	public Class<?> getComponentTypeByFieldTypeName(String additionalFieldInfo)
	{
		// H2 does not support arrays so there is never a component type
		return null;
	}

	@Override
	public String getFieldTypeNameByComponentType(Class<?> componentType)
	{
		throw new UnsupportedOperationException();
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
		sqlCommand = sqlCommand.replaceAll(Pattern.quote("NUMBER(1,0)"), "BOOLEAN");
		sqlCommand = sqlCommand.replaceAll(Pattern.quote("NUMBER(9,0)"), "INT");
		sqlCommand = sqlCommand.replaceAll(Pattern.quote("NUMBER(18,0)"), "LONG");
		sqlCommand = sqlCommand.replaceAll("VARCHAR2\\((\\d+) BYTE\\)", "VARCHAR($1)");
		sqlCommand = sqlCommand.replaceAll("VARCHAR2\\((\\d+) CHAR\\)", "VARCHAR($1)");
		sqlCommand = sqlCommand.replaceAll(Pattern.quote(" DEFERRABLE INITIALLY DEFERRED"), "");
		sqlCommand = sqlCommand.replaceAll(Pattern.quote(" NOORDER"), "");
		sqlCommand = sqlCommand.replaceAll(Pattern.quote(" USING INDEX"), "");
		sqlCommand = sqlCommand.replaceAll("MAXVALUE 9{19,} ", "MAXVALUE 999999999999999999");

		sqlCommand = sqlCommand.replaceAll("to_timestamp\\(", "TO_TIMESTAMP(");
		return sqlCommand;
	}

	@Override
	public String createOptimisticLockTrigger(Connection connection, String tableName) throws SQLException
	{
		String forTriggerName = "TR_" + tableName + "_OL";
		return "CREATE TRIGGER " + escapeName(null, forTriggerName) + " AFTER UPDATE ON " + escapeName(null, tableName) + " FOR EACH ROW CALL \""
				+ OptimisticLockTrigger.class.getName() + "\"";
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
	public List<String> getAllFullqualifiedTableNames(Connection connection, String... schemaNames) throws SQLException
	{
		Statement stm = null;
		ResultSet rs = null;
		try
		{
			stm = connection.createStatement();
			rs = stm.executeQuery("SELECT tab.table_schema, tab.table_name AS table_nam FROM INFORMATION_SCHEMA.TABLES AS tab WHERE tab.table_type='TABLE'");

			ArrayList<String> tableNames = new ArrayList<String>();
			while (rs.next())
			{
				String tableSchema = rs.getString("table_schema");
				String tableName = rs.getString("table_nam");
				tableNames.add(escapeName(tableSchema, tableName));
			}
			return tableNames;
		}
		finally
		{
			JdbcUtil.close(stm, rs);
		}
	}

	@Override
	public List<String> buildDropAllSchemaContent(Connection conn, String schemaName)
	{
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try
		{
			List<String> sql = new ArrayList<String>();

			{
				pstm = conn.prepareStatement("SELECT table_schema AS schema, index_name as name FROM INFORMATION_SCHEMA.INDEXES WHERE table_schema=?");
				pstm.setString(1, schemaName);
				//
				// stmt.execute("SELECT object_type, object_name FROM user_objects WHERE object_type IN ('FUNCTION', 'INDEX', 'PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'SEQUENCE', 'SYNONYM', 'TABLE', 'TYPE', 'VIEW')");
				rs = pstm.executeQuery();
				while (rs.next())
				{
					String schema = rs.getString("schema");
					String objectName = rs.getString("name");
					sql.add("DROP INDEX " + escapeName(schema, objectName));
				}
				JdbcUtil.close(pstm, rs);
			}
			{
				pstm = conn.prepareStatement("SELECT table_schema AS schema, table_name AS name FROM INFORMATION_SCHEMA.TABLES WHERE table_schema=?");
				pstm.setString(1, schemaName);
				rs = pstm.executeQuery();
				while (rs.next())
				{
					String tableSchema = rs.getString("schema");
					String tableName = rs.getString("name");
					sql.add("DROP TABLE " + escapeName(tableSchema, tableName) + " CASCADE CONSTRAINTS");
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
					sql.add("DROP VIEW " + escapeName(tableSchema, tableName) + " CASCADE CONSTRAINTS");
				}
				JdbcUtil.close(pstm, rs);
			}
			{
				pstm = conn.prepareStatement("SELECT alias_schema AS schema, alias_name AS name FROM INFORMATION_SCHEMA.FUNCTION_ALIASES WHERE alias_schema=?");
				pstm.setString(1, schemaName);
				//
				// stmt.execute("SELECT object_type, object_name FROM user_objects WHERE object_type IN ('FUNCTION', 'INDEX', 'PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'SEQUENCE', 'SYNONYM', 'TABLE', 'TYPE', 'VIEW')");
				rs = pstm.executeQuery();
				while (rs.next())
				{
					String schema = rs.getString("schema");
					String objectName = rs.getString("name");
					sql.add("DROP ALIAS " + escapeName(schema, objectName));
				}
				JdbcUtil.close(pstm, rs);
			}
			{
				pstm = conn
						.prepareStatement("SELECT sequence_schema AS schema, sequence_name AS name FROM INFORMATION_SCHEMA.SEQUENCES WHERE sequence_schema=?");
				pstm.setString(1, schemaName);
				//
				// stmt.execute("SELECT object_type, object_name FROM user_objects WHERE object_type IN ('FUNCTION', 'INDEX', 'PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'SEQUENCE', 'SYNONYM', 'TABLE', 'TYPE', 'VIEW')");
				rs = pstm.executeQuery();
				while (rs.next())
				{
					String schema = rs.getString("schema");
					String objectName = rs.getString("name");
					sql.add("DROP SEQUENCE " + escapeName(schema, objectName));
				}
				JdbcUtil.close(pstm, rs);
			}
			{
				pstm = conn.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.TRIGGERS WHERE trigger_schema=?");
				// pstm =
				// conn.prepareStatement("SELECT trigger_schema AS schema, trigger_name AS name FROM INFORMATION_SCHEMA.TRIGGERS WHERE trigger_schema=?");
				pstm.setString(1, schemaName);

				//
				// stmt.execute("SELECT object_type, object_name FROM user_objects WHERE object_type IN ('FUNCTION', 'INDEX', 'PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'SEQUENCE', 'SYNONYM', 'TABLE', 'TYPE', 'VIEW')");
				rs = pstm.executeQuery();
				printResultSet(rs);

				while (rs.next())
				{
					String schema = rs.getString("schema");
					String objectName = rs.getString("name");
					sql.add("DROP TRIGGER " + escapeName(schema, objectName));
				}
				JdbcUtil.close(pstm, rs);
			}
			return sql;
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(pstm, rs);
		}
	}
}
