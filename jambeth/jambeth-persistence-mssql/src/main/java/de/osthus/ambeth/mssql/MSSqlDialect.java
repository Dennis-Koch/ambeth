package de.osthus.ambeth.mssql;

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

import com.microsoft.sqlserver.jdbc.SQLServerDriver;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyMap;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IColumnEntry;
import de.osthus.ambeth.persistence.jdbc.AbstractConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.ColumnEntry;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.sql.ISqlBuilder;

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
	public IList<String> disableConstraints(Connection connection, String... schemaNames)
	{
		Statement stm = null;
		try
		{
			// List<String> allTableNames = getAllFullqualifiedTableNames(connection, schemaNames);
			// ArrayList<String[]> sql = new ArrayList<String[]>(allTableNames.size());
			// stm = connection.createStatement();
			// for (int i = allTableNames.size(); i-- > 0;)
			// {
			// String tableName = allTableNames.get(i);
			// String disableSql = "ALTER TABLE " + tableName + " SET REFERENTIAL_INTEGRITY FALSE";
			// sql.add(new String[] { disableSql, tableName });
			//
			// stm.addBatch(disableSql);
			// }
			// stm.executeBatch();
			// return sql;
			throw new UnsupportedOperationException("not yet implemented");
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(stm);
		}
	}

	@Override
	public void enableConstraints(Connection connection, IList<String> disabled)
	{
		if (disabled == null || disabled.isEmpty())
		{
			return;
		}
		Statement stm = null;
		try
		{
			// stm = connection.createStatement();
			// for (int i = disabled.size(); i-- > 0;)
			// {
			// String tableName = disabled.get(i)[1];
			//
			// stm.addBatch("ALTER TABLE " + tableName + " SET REFERENTIAL_INTEGRITY TRUE CHECK");
			// }
			// stm.executeBatch();
			throw new UnsupportedOperationException("not yet implemented");
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(stm);
		}
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
	public List<String> getAllFullqualifiedSequences(Connection connection)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	protected void handleRow(String schemaName, String tableName, String constraintName, ArrayList<String> disableConstraintsSQL,
			ArrayList<String> enableConstraintsSQL)
	{
		throw new UnsupportedOperationException();
	}
}
