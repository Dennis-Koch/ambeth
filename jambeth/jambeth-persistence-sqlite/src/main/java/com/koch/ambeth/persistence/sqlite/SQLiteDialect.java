package com.koch.ambeth.persistence.sqlite;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;

import org.sqlite.JDBC;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.persistence.IColumnEntry;
import com.koch.ambeth.persistence.SelectPosition;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.connection.IConnectionKeyHandle;
import com.koch.ambeth.persistence.jdbc.AbstractConnectionDialect;
import com.koch.ambeth.persistence.jdbc.ColumnEntry;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.sql.LimitByLimitOperator;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IValueOperand;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.LinkedHashMap;

public class SQLiteDialect extends AbstractConnectionDialect
{
	// protected static final LinkedHashMap<Class<?>, String[]> typeToArrayTypeNameMap = new LinkedHashMap<Class<?>, String[]>(128, 0.5f);
	//
	// protected static final LinkedHashMap<String, Class<?>> arrayTypeNameToTypeMap = new LinkedHashMap<String, Class<?>>(128, 0.5f);
	//
	// static
	// {
	// typeToArrayTypeNameMap.put(Long.TYPE, new String[] { "bigint[]", "bigint" });
	// typeToArrayTypeNameMap.put(Long.class, new String[] { "bigint[]", "bigint" });
	// typeToArrayTypeNameMap.put(Integer.TYPE, new String[] { "integer[]", "integer" });
	// typeToArrayTypeNameMap.put(Integer.class, new String[] { "integer[]", "integer" });
	// typeToArrayTypeNameMap.put(Short.TYPE, new String[] { "smallint[]", "smallint" });
	// typeToArrayTypeNameMap.put(Short.class, new String[] { "smallint[]", "smallint" });
	// typeToArrayTypeNameMap.put(Byte.TYPE, new String[] { "smallint[]", "smallint" });
	// typeToArrayTypeNameMap.put(Byte.class, new String[] { "smallint[]", "smallint" });
	// typeToArrayTypeNameMap.put(Character.TYPE, new String[] { "char", "char" });
	// typeToArrayTypeNameMap.put(Character.class, new String[] { "char", "char" });
	// typeToArrayTypeNameMap.put(Boolean.TYPE, new String[] { "boolean[]", "boolean" });
	// typeToArrayTypeNameMap.put(Boolean.class, new String[] { "boolean[]", "boolean" });
	// typeToArrayTypeNameMap.put(Double.TYPE, new String[] { "double precision[]", "double precision" });
	// typeToArrayTypeNameMap.put(Double.class, new String[] { "double precision[]", "double precision" });
	// typeToArrayTypeNameMap.put(Float.TYPE, new String[] { "real[]", "real" });
	// typeToArrayTypeNameMap.put(Float.class, new String[] { "real[]", "real" });
	// typeToArrayTypeNameMap.put(String.class, new String[] { "text[]", "text" });
	// typeToArrayTypeNameMap.put(BigDecimal.class, new String[] { "numeric[]", "numeric" });
	// typeToArrayTypeNameMap.put(BigInteger.class, new String[] { "numeric[]", "numeric" });
	//
	// // Default behavior. This is an intended "hack" for backwards compatibility.
	// typeToArrayTypeNameMap.put(Object.class, new String[] { "numeric[]", "numeric" });
	//
	// for (Entry<Class<?>, String[]> entry : typeToArrayTypeNameMap)
	// {
	// arrayTypeNameToTypeMap.putIfNotExists(entry.getValue()[0], entry.getKey());
	// }
	// }

	public static int getOptimisticLockErrorCode()
	{
		return 20800;
	}

	public static int getPessimisticLockErrorCode()
	{
		// 54 = RESOURCE BUSY acquiring with NOWAIT (pessimistic lock)
		return 54;
	}

	@LogInstance
	private ILogger log;

	protected final DateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

	protected final WeakHashMap<IConnectionKeyHandle, ConnectionKeyValue> connectionToConstraintSqlMap = new WeakHashMap<IConnectionKeyHandle, ConnectionKeyValue>();

	@Autowired
	protected IServiceContext serviceContext;

	@Autowired
	protected ILoggerHistory loggerHistory;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Autowired(optional = true)
	protected ITransactionState transactionState;

	@Property(name = PersistenceConfigurationConstants.ExternalTransactionManager, defaultValue = "false")
	protected boolean externalTransactionManager;

	@Property(name = PersistenceConfigurationConstants.AutoIndexForeignKeys, defaultValue = "false")
	protected boolean autoIndexForeignKeys;

	@Property(name = PersistenceConfigurationConstants.AutoArrayTypes, defaultValue = "true")
	protected boolean autoArrayTypes;

	protected final Lock readLock, writeLock;

	public SQLiteDialect()
	{
		ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		readLock = rwLock.readLock();
		writeLock = rwLock.writeLock();
	}

	@Override
	protected Class<?> getDriverType()
	{
		return JDBC.class;
	}

	@Override
	public int getMaxInClauseBatchThreshold()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	protected String buildDeferrableForeignKeyConstraintsSelectSQL(String[] schemaNames)
	{
		StringBuilder sb = new StringBuilder(
				"SELECT n.nspname AS OWNER, cl.relname AS TABLE_NAME, c.conname AS CONSTRAINT_NAME FROM pg_constraint c JOIN pg_namespace n ON c.connamespace=n.oid JOIN pg_class cl ON c.conrelid=cl.oid WHERE c.condeferrable='t' AND c.condeferred='f' AND n.nspname");
		buildSchemaInClause(sb, schemaNames);
		return sb.toString();
	}

	@Override
	public IList<IMap<String, String>> getExportedKeys(Connection connection, String[] schemaNames) throws SQLException
	{
		ArrayList<IMap<String, String>> allForeignKeys = new ArrayList<IMap<String, String>>();
		PreparedStatement pstm = null;
		ResultSet allForeignKeysRS = null;
		try
		{
			String[] newSchemaNames = new String[schemaNames.length];
			System.arraycopy(schemaNames, 0, newSchemaNames, 0, schemaNames.length);
			for (int a = newSchemaNames.length; a-- > 0;)
			{
				newSchemaNames[a] = newSchemaNames[a].toLowerCase();
			}
			String subselect = "SELECT ns.nspname AS \"owner\", con1.conname AS \"constraint_name\", unnest(con1.conkey) AS \"parent\", unnest(con1.confkey) AS \"child\", con1.confrelid, con1.conrelid"//
					+ " FROM pg_class cl"//
					+ " JOIN pg_namespace ns ON cl.relnamespace=ns.oid"//
					+ " JOIN pg_constraint con1 ON con1.conrelid=cl.oid"//
					+ " WHERE con1.contype='f' AND ns.nspname" + buildSchemaInClause(newSchemaNames);

			String sql = "select owner, constraint_name, cl2.relname as \"fk_table\", att2.attname as \"fk_column\", cl.relname as \"pk_table\", att.attname as \"pk_column\""//
					+ " from (" + subselect + ") con"//
					+ " JOIN pg_attribute att ON att.attrelid=con.confrelid AND att.attnum=con.child"//
					+ " JOIN pg_class cl ON cl.oid=con.confrelid"//
					+ " JOIN pg_class cl2 ON cl2.oid=con.conrelid"//
					+ " JOIN pg_attribute att2 ON att2.attrelid = con.conrelid AND att2.attnum=con.parent";
			pstm = connection.prepareStatement(sql);
			allForeignKeysRS = pstm.executeQuery();
			while (allForeignKeysRS.next())
			{
				HashMap<String, String> foreignKey = new HashMap<String, String>();

				foreignKey.put("OWNER", allForeignKeysRS.getString("owner"));
				foreignKey.put("CONSTRAINT_NAME", allForeignKeysRS.getString("constraint_name"));
				foreignKey.put("FKTABLE_NAME", allForeignKeysRS.getString("fk_table"));
				foreignKey.put("FKCOLUMN_NAME", allForeignKeysRS.getString("fk_column"));
				foreignKey.put("PKTABLE_NAME", allForeignKeysRS.getString("pk_table"));
				foreignKey.put("PKCOLUMN_NAME", allForeignKeysRS.getString("pk_column"));

				allForeignKeys.add(foreignKey);
			}
		}
		finally
		{
			JdbcUtil.close(pstm, allForeignKeysRS);
		}
		return allForeignKeys;
	}

	@Override
	public ILinkedMap<String, IList<String>> getFulltextIndexes(Connection connection, String schemaName) throws SQLException
	{
		LinkedHashMap<String, IList<String>> fulltextIndexes = new LinkedHashMap<String, IList<String>>();
		// NOT YET IMPLEMENTED
		return fulltextIndexes;
	}

	@Override
	public boolean isSystemTable(String tableName)
	{
		return false;
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint, Connection connection) throws SQLException
	{
	}

	@Override
	public int getResourceBusyErrorCode()
	{
		return 54;
	}

	@Override
	public PersistenceException createPersistenceException(SQLException e, String relatedSql)
	{
		PersistenceException ex = new PersistenceException(relatedSql, e);
		ex.setStackTrace(e.getStackTrace());

		return ex;
		// String sqlState = e.getSQLState();
		//
		// if (SQLState.NULL_CONSTRAINT.getXopen().equals(sqlState))
		// {
		// NullConstraintException ex = new NullConstraintException(e.getMessage(), relatedSql, e);
		// ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
		// return ex;
		// }
		// else if (SQLState.UNIQUE_CONSTRAINT.getXopen().equals(sqlState))
		// {
		// UniqueConstraintException ex = new UniqueConstraintException(e.getMessage(), relatedSql, e);
		// ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
		// return ex;
		// }
		// int errorCode = e.getErrorCode();
		//
		// if (e.getMessage().contains("" + getOptimisticLockErrorCode()))
		// {
		// System.out.println("efkwoefwef");
		// }
		//
		// if (errorCode == getPessimisticLockErrorCode())
		// {
		// PessimisticLockException ex = new PessimisticLockException(relatedSql, e);
		// ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
		// return ex;
		// }
		// if (errorCode == getOptimisticLockErrorCode())
		// {
		// OptimisticLockException ex = new OptimisticLockException(relatedSql, e);
		// ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
		// return ex;
		// }
		// if (errorCode == 1400)
		// {
		// }
		//
		// PersistenceException ex = new PersistenceException(relatedSql, e);
		// ex.setStackTrace(e.getStackTrace());
		//
		// return ex;
	}

	@Override
	public ResultSet getIndexInfo(Connection connection, String schemaName, String tableName, boolean unique) throws SQLException
	{
		return connection.getMetaData().getIndexInfo(null, schemaName, tableName, unique, true);
	}

	@Override
	public Class<?> getComponentTypeByFieldTypeName(String fieldTypeName)
	{
		if (fieldTypeName == null)
		{
			return null;
		}
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
		// return arrayTypeNameToTypeMap.get(fieldTypeName);
	}

	@Override
	public String getFieldTypeNameByComponentType(Class<?> componentType)
	{
		if (componentType == null)
		{
			return null;
		}
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
		// String[] fieldTypeName = typeToArrayTypeNameMap.get(componentType);
		// if (fieldTypeName == null)
		// {
		// throw new IllegalArgumentException("Can not handle component type '" + componentType + "'");
		// }
		// return fieldTypeName[0];
	}

	@Override
	public List<String> getAllFullqualifiedSequences(Connection connection, String... schemaNames) throws SQLException
	{
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
		// List<String> allSequenceNames = new ArrayList<String>();
		//
		// Statement stmt = null;
		// ResultSet rs = null;
		// try
		// {
		// stmt = connection.createStatement();
		// rs = stmt.executeQuery("SELECT t.sequence_schema || '.' || t.sequence_name FROM information_schema.sequences t WHERE t.sequence_schema"
		// + buildSchemaInClause(schemaNames));
		// while (rs.next())
		// {
		// String fqSequenceName = rs.getString(1);
		// allSequenceNames.add(fqSequenceName);
		// }
		// }
		// catch (Throwable e)
		// {
		// throw RuntimeExceptionUtil.mask(e);
		// }
		// finally
		// {
		// JdbcUtil.close(stmt, rs);
		// }
		// return allSequenceNames;
	}

	@Override
	public List<String> getAllFullqualifiedTableNames(Connection connection, String... schemaNames) throws SQLException
	{
		return queryDefault(connection, "FULL_NAME", "SELECT tbl_name AS FULL_NAME FROM sqlite_master where type='table'");
	}

	@Override
	public List<String> getAllFullqualifiedViews(Connection connection, String... schemaNames) throws SQLException
	{
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
		//
		// List<String> allViewNames = new ArrayList<String>();
		//
		// Statement stmt = null;
		// ResultSet rs = null;
		// try
		// {
		// for (String schemaName : schemaNames)
		// {
		// rs = connection.getMetaData().getTables(null, schemaName, null, new String[] { "VIEW" });
		//
		// while (rs.next())
		// {
		// // String schemaName = rs.getString("TABLE_SCHEM");
		// String viewName = rs.getString("TABLE_NAME");
		// if (!BIN_TABLE_NAME.matcher(viewName).matches() && !IDX_TABLE_NAME.matcher(viewName).matches())
		// {
		// allViewNames.add(schemaName + "." + viewName);
		// }
		// }
		// }
		// }
		// finally
		// {
		// JdbcUtil.close(stmt, rs);
		// }
		//
		// return allViewNames;
	}

	@Override
	protected void handleRow(String schemaName, String tableName, String constraintName, com.koch.ambeth.util.collections.ArrayList<String> disableConstraintsSQL,
			com.koch.ambeth.util.collections.ArrayList<String> enableConstraintsSQL)
	{
		String fullName = "\"" + schemaName + "\".\"" + constraintName + "\"";
		disableConstraintsSQL.add("SET CONSTRAINTS " + fullName + " DEFERRED");
		enableConstraintsSQL.add("SET CONSTRAINTS " + fullName + " IMMEDIATE");
	}

	@Override
	public IList<IColumnEntry> getAllFieldsOfTable(Connection connection, String fqTableName) throws SQLException
	{
		String[] names = sqlBuilder.getSchemaAndTableName(fqTableName);
		ResultSet tableColumnsRS = connection.getMetaData().getColumns(null, names[0], names[1], null);
		try
		{
			ArrayList<IColumnEntry> columns = new ArrayList<IColumnEntry>();
			columns.add(new ColumnEntry("ctid", -1, Object.class, null, false, 0, false));

			while (tableColumnsRS.next())
			{
				String fieldName = tableColumnsRS.getString("COLUMN_NAME");
				int columnIndex = tableColumnsRS.getInt("ORDINAL_POSITION");
				int typeIndex = tableColumnsRS.getInt("DATA_TYPE");

				String typeName = tableColumnsRS.getString("TYPE_NAME");
				while (typeName.startsWith("_"))
				{
					typeName = typeName.substring(1) + "[]";
				}
				String isNullable = tableColumnsRS.getString("IS_NULLABLE");
				boolean nullable = "YES".equalsIgnoreCase(isNullable);

				int scale = tableColumnsRS.getInt("COLUMN_SIZE");
				int digits = tableColumnsRS.getInt("DECIMAL_DIGITS");
				int radix = tableColumnsRS.getInt("NUM_PREC_RADIX");

				Class<?> javaType = JdbcUtil.getJavaTypeFromJdbcType(typeIndex, scale, digits);
				if ("lo".equalsIgnoreCase(typeName))
				{
					javaType = Blob.class;
				}
				else if ("text".equalsIgnoreCase(typeName))
				{
					javaType = String.class;
				}
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
	public boolean isTransactionNecessaryDuringLobStreaming()
	{
		return true;
	}

	@Override
	public String prepareCommand(String sqlCommand)
	{
		Pattern seqPattern = Pattern.compile("CREATE\\s+SEQUENCE\\s+([\\S]+)\\s+(.+)");
		Matcher matcher = seqPattern.matcher(sqlCommand);
		if (matcher.matches())
		{
			String seqName = matcher.group(1);
			return "CREATE TABLE " + seqName + " (integer curr_value)";
		}
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *1 *, *0 *\\)", " INTEGER");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *[0-9] *, *0 *\\)", " INTEGER");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *1[0,1,2,3,4,5,6,7,8] *, *0 *\\)", " INTEGER");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *\\d+ *\\, *\\d+ *\\)", " REAL");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *\\* *\\, *\\d+ *\\)", " REAL");
		sqlCommand = prepareCommandIntern(sqlCommand, " NUMBER *\\( *\\d+ *\\)", " REAL");
		sqlCommand = prepareCommandInternWithGroup(sqlCommand, " NUMBER([^\"])", " REAL");
		// sqlCommand = prepareCommandIntern(sqlCommand, "(?: |\")NUMBER *\\(", " NUMERIC\\(");

		sqlCommand = prepareCommandInternWithGroup(sqlCommand, " VARCHAR *\\( *(\\d+) +CHAR *\\)", " TEXT");

		sqlCommand = prepareCommandInternWithGroup(sqlCommand, " VARCHAR2 *\\( *(\\d+) +BYTE\\)", " TEXT");
		sqlCommand = prepareCommandInternWithGroup(sqlCommand, " VARCHAR2 *\\( *(\\d+) +CHAR\\)", " TEXT");

		sqlCommand = prepareCommandInternWithGroup(sqlCommand, " PRIMARY KEY (\\([^\\)]+\\)) USING INDEX", " PRIMARY KEY \\2");
		sqlCommand = prepareCommandInternWithGroup(sqlCommand, " PRIMARY KEY (\\([^\\)]+\\)) USING INDEX", " PRIMARY KEY \\2");

		sqlCommand = prepareCommandInternWithGroup(sqlCommand, "([^a-zA-Z0-9])STRING_ARRAY([^a-zA-Z0-9])", "\\2TEXT[]\\3");
		if (sqlCommand.endsWith(" CASCADE"))
		{
			sqlCommand = sqlCommand.substring(0, sqlCommand.length() - " CASCADE".length());
		}
		sqlCommand = prepareCommandInternWithGroup(sqlCommand, "to_timestamp\\('([^']+)','([^']+)'\\)", "strftime('%s','\\2')");
		if (sqlCommand.startsWith("CREATE OR REPLACE TYPE "))
		{
			return "";
		}
		return sqlCommand;
	}

	@Override
	public IOperand getLimitOperand(IOperand operand, IValueOperand valueOperand)
	{
		return beanContext.registerBean(LimitByLimitOperator.class)//
				.propertyValue("Operand", operand)//
				.propertyValue("ValueOperand", operand)//
				.finish();
	}

	@Override
	protected ConnectionKeyValue preProcessConnectionIntern(Connection connection, String[] schemaNames, boolean forcePreProcessing) throws SQLException
	{
		Statement stm = connection.createStatement();
		try
		{
			stm.execute("PRAGMA foreign_keys = ON");
		}
		finally
		{
			JdbcUtil.close(stm);
		}
		return super.preProcessConnectionIntern(connection, schemaNames, forcePreProcessing);
	}

	// @Override
	// protected ConnectionKeyValue preProcessConnectionIntern(Connection connection, String[] schemaNames, boolean forcePreProcessing) throws SQLException
	// {
	// Statement stm = null;
	// try
	// {
	// stm = connection.createStatement();
	// stm.execute("SET SCHEMA '" + toDefaultCase(schemaNames[0]) + "'");
	// }
	// catch (Throwable e)
	// {
	// throw RuntimeExceptionUtil.mask(e);
	// }
	// finally
	// {
	// JdbcUtil.close(stm);
	// }
	// return scanForUndeferredDeferrableConstraints(connection, schemaNames);
	// }

	@Override
	public SelectPosition getLimitPosition()
	{
		return SelectPosition.AFTER_WHERE;
	}
}
