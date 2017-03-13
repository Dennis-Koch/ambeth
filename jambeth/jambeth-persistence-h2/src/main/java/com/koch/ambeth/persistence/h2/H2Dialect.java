package com.koch.ambeth.persistence.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import org.h2.Driver;

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
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class H2Dialect extends AbstractConnectionDialect
{
	public static int getOptimisticLockErrorCode()
	{
		return 10001;
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
		return Driver.class;
	}

	@Override
	protected ConnectionKeyValue preProcessConnectionIntern(Connection connection, String[] schemaNames, boolean forcePreProcessing) throws SQLException
	{
		Statement stm = null;
		ResultSet rs = null;
		try
		{
			stm = connection.createStatement();
			stm.execute("SET MULTI_THREADED 1");

			if ("jdbc:h2:mem".equals(protocol))
			{
				stm.execute("SET DB_CLOSE_DELAY -1");
			}
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

			stm = connection.createStatement();
			ArrayList<String> disableSql = new ArrayList<String>();
			ArrayList<String> enableSql = new ArrayList<String>();
			for (String tableName : getAllFullqualifiedTableNames(connection, schemaNames))
			{
				disableSql.add("ALTER TABLE " + tableName + " SET REFERENTIAL_INTEGRITY FALSE");
				enableSql.add("ALTER TABLE " + tableName + " SET REFERENTIAL_INTEGRITY TRUE CHECK");
			}
			return new ConnectionKeyValue(disableSql.toArray(String.class), enableSql.toArray(String.class));
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
		stm.execute("CREATE ALIAS \"" + toDefaultCase(aliasName) + "\" FOR \"" + functionName + "\"");
	}

	@Override
	public IList<IMap<String, String>> getExportedKeys(Connection connection, String[] schemaNames) throws SQLException
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

				foreignKey.put("OWNER", schemaNames[0]);
				foreignKey.put("CONSTRAINT_NAME", rs.getString("FK_NAME"));
				foreignKey.put("FKTABLE_NAME", rs.getString("FKTABLE_NAME"));
				foreignKey.put("FKCOLUMN_NAME", rs.getString("FKCOLUMN_NAME"));
				foreignKey.put("PKTABLE_NAME", rs.getString("PKTABLE_NAME"));
				foreignKey.put("PKCOLUMN_NAME", rs.getString("PKCOLUMN_NAME"));

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
		// H2 does not support arrays so there is never a component type
		return null;
	}

	@Override
	public String getFieldTypeNameByComponentType(Class<?> componentType)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getAllFullqualifiedSequences(Connection connection, String... schemaNames) throws SQLException
	{
		if (schemaNames.length == 0)
		{
			schemaNames = this.schemaNames;
		}
		return queryDefault(connection, "full_name",
				"SELECT sequence_schema || '.' || sequence_name AS full_name FROM INFORMATION_SCHEMA.SEQUENCES WHERE sequence_schema IN (?)",
				(Object) schemaNames);
	}

	@Override
	public List<String> getAllFullqualifiedTableNames(Connection connection, String... schemaNames) throws SQLException
	{
		if (schemaNames.length == 0)
		{
			schemaNames = this.schemaNames;
		}
		return queryDefault(connection, "full_name",
				"SELECT table_schema || '.' || table_name AS full_name FROM INFORMATION_SCHEMA.TABLES WHERE table_type='TABLE' AND table_schema IN (?)",
				(Object) schemaNames);
	}

	@Override
	public List<String> getAllFullqualifiedViews(Connection connection, String... schemaNames) throws SQLException
	{
		if (schemaNames.length == 0)
		{
			schemaNames = this.schemaNames;
		}
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try
		{
			pstm = connection.prepareStatement("SELECT table_schema, table_name FROM INFORMATION_SCHEMA.VIEWS WHERE table_schema IN (?)");
			pstm.setObject(1, schemaNames);
			rs = pstm.executeQuery();
			ArrayList<String> viewNames = new ArrayList<String>();
			while (rs.next())
			{
				String tableSchema = rs.getString("table_schema");
				String tableName = rs.getString("table_name");
				viewNames.add(tableSchema + "." + tableName);
			}
			return viewNames;
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
	public SelectPosition getLimitPosition()
	{
		return SelectPosition.AFTER_WHERE;
	}
}
