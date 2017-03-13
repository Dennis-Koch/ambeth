package com.koch.ambeth.persistence.mssql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.IColumnEntry;
import com.koch.ambeth.persistence.SelectPosition;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.jdbc.AbstractConnectionDialect;
import com.koch.ambeth.persistence.jdbc.ColumnEntry;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyMap;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;

public class MSSqlDialect extends AbstractConnectionDialect
{
	public static final Pattern BIN_TABLE_NAME = Pattern.compile("BIN\\$.{22}==\\$0", Pattern.CASE_INSENSITIVE);

	public static final Pattern IDX_TABLE_NAME = Pattern.compile("DR\\$.*?\\$.", Pattern.CASE_INSENSITIVE);

	public static int getOptimisticLockErrorCode()
	{
		return 20800;
	}

	public static int getPessimisticLockErrorCode()
	{
		// 54 = RESOURCE BUSY acquiring with NOWAIT (pessimistic lock)
		return 54;
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseProtocol)
	protected String protocol;

	@Override
	protected Class<?> getDriverType()
	{
		return SQLServerDriver.class;
	}

	@Override
	public IList<IMap<String, String>> getExportedKeys(Connection connection, String[] schemaNames) throws SQLException
	{
		Statement stm = null;
		ResultSet rs = null;
		try
		{
			// ArrayList<IMap<String, String>> allForeignKeys = new ArrayList<IMap<String, String>>();
			// return allForeignKeys;
			throw new UnsupportedOperationException("not yet implemented");
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
	public void releaseSavepoint(Savepoint savepoint, Connection connection) throws SQLException
	{
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public String getFieldTypeNameByComponentType(Class<?> componentType)
	{
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public List<String> getAllFullqualifiedTableNames(Connection connection, String... schemaNames) throws SQLException
	{
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try
		{
			// pstm = connection
			// .prepareStatement("SELECT table_schema, table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_type='TABLE' AND table_schema IN (?)");
			// pstm.setObject(1, schemaNames);
			// rs = pstm.executeQuery();
			// ArrayList<String> tableNames = new ArrayList<String>();
			// while (rs.next())
			// {
			// String tableSchema = rs.getString("table_schema");
			// String tableName = rs.getString("table_name");
			// tableNames.add(tableSchema + "." + tableName);
			// }
			// return tableNames;
			throw new UnsupportedOperationException("not yet implemented");
		}
		finally
		{
			JdbcUtil.close(pstm, rs);
		}
	}

	@Override
	public List<String> getAllFullqualifiedViews(Connection connection, String... schemaNames) throws SQLException
	{
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try
		{
			// pstm = connection.prepareStatement("SELECT table_schema, table_name FROM INFORMATION_SCHEMA.VIEWS WHERE table_schema IN (?)");
			// pstm.setObject(1, schemaNames);
			// rs = pstm.executeQuery();
			// ArrayList<String> viewNames = new ArrayList<String>();
			// while (rs.next())
			// {
			// String tableSchema = rs.getString("table_schema");
			// String tableName = rs.getString("table_name");
			// viewNames.add(tableSchema + "." + tableName);
			// }
			// return viewNames;
			throw new UnsupportedOperationException("not yet implemented");
		}
		finally
		{
			JdbcUtil.close(pstm, rs);
		}
	}

	@Override
	public IList<IColumnEntry> getAllFieldsOfTable(Connection connection, String fqTableName) throws SQLException
	{
		String[] names = sqlBuilder.getSchemaAndTableName(fqTableName);
		ResultSet tableColumnsRS = connection.getMetaData().getColumns(null, names[0], names[1], null);
		try
		{
			ArrayList<IColumnEntry> columns = new ArrayList<IColumnEntry>();
			while (tableColumnsRS.next())
			{
				String fieldName = tableColumnsRS.getString("COLUMN_NAME");
				int columnIndex = tableColumnsRS.getInt("ORDINAL_POSITION");
				int typeIndex = tableColumnsRS.getInt("DATA_TYPE");

				String typeName = tableColumnsRS.getString("TYPE_NAME");

				String isNullable = tableColumnsRS.getString("IS_NULLABLE");
				boolean nullable = "YES".equalsIgnoreCase(isNullable);

				int scale = tableColumnsRS.getInt("COLUMN_SIZE");
				int digits = tableColumnsRS.getInt("DECIMAL_DIGITS");
				int radix = tableColumnsRS.getInt("NUM_PREC_RADIX");

				Class<?> javaType = JdbcUtil.getJavaTypeFromJdbcType(typeIndex, scale, digits);

				ColumnEntry entry = new ColumnEntry(fieldName, columnIndex, javaType, typeName, nullable, radix, true);
				columns.add(entry);
			}
			return columns;
		}
		finally
		{
			JdbcUtil.close(tableColumnsRS);
		}
	}

	@Override
	protected String buildDeferrableForeignKeyConstraintsSelectSQL(String[] schemaNames)
	{
		return null;
	}

	@Override
	public List<String> getAllFullqualifiedSequences(Connection connection, String... schemaNames) throws SQLException
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	protected void handleRow(String schemaName, String tableName, String constraintName, ArrayList<String> disableConstraintsSQL,
			ArrayList<String> enableConstraintsSQL)
	{
		throw new UnsupportedOperationException();
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
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER", " REAL");
		sqlCommand = prepareCommandIntern(sqlCommand, " DEFERRABLE INITIALLY DEFERRED", "");

		sqlCommand = prepareCommandInternWithGroup(sqlCommand, " VARCHAR2 *\\( *(\\d+) +BYTE\\)", " VARCHAR(\\2)");

		sqlCommand = prepareCommandInternWithGroup(sqlCommand, " PRIMARY KEY (\\([^\\)]+\\)) USING INDEX", " PRIMARY KEY \\2");

		return sqlCommand;
	}

	@Override
	public SelectPosition getLimitPosition()
	{
		return SelectPosition.PRE_COLUMNS;
	}
}
