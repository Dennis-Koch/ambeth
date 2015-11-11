package de.osthus.ambeth.pg;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.persistence.PessimisticLockException;

import org.postgresql.Driver;
import org.postgresql.PGConnection;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.ILoggerHistory;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.persistence.ArrayQueryItem;
import de.osthus.ambeth.persistence.IColumnEntry;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IFieldMetaData;
import de.osthus.ambeth.persistence.SQLState;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.exception.NullConstraintException;
import de.osthus.ambeth.persistence.exception.UniqueConstraintException;
import de.osthus.ambeth.persistence.jdbc.AbstractConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.ColumnEntry;
import de.osthus.ambeth.persistence.jdbc.IConnectionExtension;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.connection.IConnectionKeyHandle;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.sql.ParamsUtil;

public class PostgresDialect extends AbstractConnectionDialect
{
	public static final Pattern BIN_TABLE_NAME = Pattern.compile("BIN\\$.{22}==\\$0", Pattern.CASE_INSENSITIVE);

	public static final Pattern IDX_TABLE_NAME = Pattern.compile("DR\\$.*?\\$.", Pattern.CASE_INSENSITIVE);

	protected static final LinkedHashMap<Class<?>, String[]> typeToArrayTypeNameMap = new LinkedHashMap<Class<?>, String[]>(128, 0.5f);

	protected static final LinkedHashMap<String, Class<?>> arrayTypeNameToTypeMap = new LinkedHashMap<String, Class<?>>(128, 0.5f);

	static
	{
		typeToArrayTypeNameMap.put(Long.TYPE, new String[] { "bigint[]", "bigint" });
		typeToArrayTypeNameMap.put(Long.class, new String[] { "bigint[]", "bigint" });
		typeToArrayTypeNameMap.put(Integer.TYPE, new String[] { "integer[]", "integer" });
		typeToArrayTypeNameMap.put(Integer.class, new String[] { "integer[]", "integer" });
		typeToArrayTypeNameMap.put(Short.TYPE, new String[] { "smallint[]", "smallint" });
		typeToArrayTypeNameMap.put(Short.class, new String[] { "smallint[]", "smallint" });
		typeToArrayTypeNameMap.put(Byte.TYPE, new String[] { "smallint[]", "smallint" });
		typeToArrayTypeNameMap.put(Byte.class, new String[] { "smallint[]", "smallint" });
		typeToArrayTypeNameMap.put(Character.TYPE, new String[] { "char", "char" });
		typeToArrayTypeNameMap.put(Character.class, new String[] { "char", "char" });
		typeToArrayTypeNameMap.put(Boolean.TYPE, new String[] { "boolean[]", "boolean" });
		typeToArrayTypeNameMap.put(Boolean.class, new String[] { "boolean[]", "boolean" });
		typeToArrayTypeNameMap.put(Double.TYPE, new String[] { "double precision[]", "double precision" });
		typeToArrayTypeNameMap.put(Double.class, new String[] { "double precision[]", "double precision" });
		typeToArrayTypeNameMap.put(Float.TYPE, new String[] { "real[]", "real" });
		typeToArrayTypeNameMap.put(Float.class, new String[] { "real[]", "real" });
		typeToArrayTypeNameMap.put(String.class, new String[] { "text[]", "text" });
		typeToArrayTypeNameMap.put(BigDecimal.class, new String[] { "numeric[]", "numeric" });
		typeToArrayTypeNameMap.put(BigInteger.class, new String[] { "numeric[]", "numeric" });

		// Default behavior. This is an intended "hack" for backwards compatibility.
		typeToArrayTypeNameMap.put(Object.class, new String[] { "numeric[]", "numeric" });

		for (Entry<Class<?>, String[]> entry : typeToArrayTypeNameMap)
		{
			arrayTypeNameToTypeMap.putIfNotExists(entry.getValue()[0], entry.getKey());
		}
	}

	public static int getOptimisticLockErrorCode()
	{
		return 20800;
	}

	public static int getPessimisticLockErrorCode()
	{
		// 54 = RESOURCE BUSY acquiring with NOWAIT (pessimistic lock)
		return 54;
	}

	public static boolean isBLobColumnName(String typeName)
	{
		return "lo".equals(typeName);
	}

	public static boolean isCLobColumnName(String typeName)
	{
		return false;// "text".equals(typeName);
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

	public PostgresDialect()
	{
		ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		readLock = rwLock.readLock();
		writeLock = rwLock.writeLock();
	}

	@Override
	protected Class<?> getDriverType()
	{
		return Driver.class;
	}

	@Override
	public IOperand getRegexpLikeFunction(IOperand sourceString, IOperand pattern, IOperand matchParameter)
	{
		return beanContext.registerBean(PgSqlRegexpLikeOperand.class).propertyValue("SourceString", sourceString).propertyValue("Pattern", pattern)
				.propertyValue("MatchParameter", matchParameter).finish();
	}

	@Override
	public String toDefaultCase(String identifier)
	{
		return identifier.toLowerCase();
	}

	@Override
	public boolean isCompactMultiValueRecommended(IList<Object> values)
	{
		return true;
	}

	@Override
	public void handleWithMultiValueLeftField(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters,
			IList<IList<Object>> splitValues, boolean caseSensitive, Class<?> leftOperandFieldType)
	{
		if (splitValues.size() == 0)
		{
			// Special scenario with EMPTY argument
			ArrayQueryItem aqi = new ArrayQueryItem(new Object[0], leftOperandFieldType);
			ParamsUtil.addParam(parameters, aqi);
			querySB.append("SELECT ");
			if (!caseSensitive)
			{
				querySB.append("LOWER(");
			}
			querySB.append("COLUMN_VALUE");
			if (!caseSensitive)
			{
				querySB.append(") COLUMN_VALUE");
			}
			querySB.append(" FROM UNNEST(ARRAY[?]) COLUMN_VALUE");
		}
		else
		{
			String placeholder;
			if (caseSensitive)
			{
				placeholder = "COLUMN_VALUE";
			}
			else
			{
				placeholder = "LOWER(COLUMN_VALUE) COLUMN_VALUE";
			}

			for (int a = 0, size = splitValues.size(); a < size; a++)
			{
				IList<Object> values = splitValues.get(a);
				if (a > 0)
				{
					// A union allows us to suppress the "ROWNUM" column because table(?) will already get materialized without it
					querySB.append(" UNION ");
				}
				if (size > 1)
				{
					querySB.append('(');
				}
				ArrayQueryItem aqi = new ArrayQueryItem(values.toArray(), leftOperandFieldType);
				ParamsUtil.addParam(parameters, aqi);

				querySB.append("SELECT ").append(placeholder);
				if (size < 2)
				{
					// No union active
					// querySB.append(",ROWNUM");
				}
				querySB.append(" FROM UNNEST(ARRAY[?]) COLUMN_VALUE");
				if (size > 1)
				{
					querySB.append(')');
				}
			}
		}
	}

	@Override
	public int getMaxInClauseBatchThreshold()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public Blob createBlob(Connection connection) throws SQLException
	{
		PGConnection pgConnection = connection.unwrap(PGConnection.class);
		long oid = pgConnection.getLargeObjectAPI().createLO();
		return new PostgresBlob(pgConnection, oid);
	}

	@Override
	public Clob createClob(Connection connection) throws SQLException
	{
		PGConnection pgConnection = connection.unwrap(PGConnection.class);
		long oid = pgConnection.getLargeObjectAPI().createLO();
		return new PostgresClob(pgConnection, oid);
	}

	@Override
	public Object convertToFieldType(IFieldMetaData field, Object value)
	{
		if (isBLobColumnName(field.getOriginalTypeName()))
		{
			return conversionHelper.convertValueToType(Blob.class, value, field.getFieldSubType());
		}
		else if (isCLobColumnName(field.getOriginalTypeName()))
		{
			return conversionHelper.convertValueToType(Clob.class, value, field.getFieldSubType());
		}
		return super.convertToFieldType(field, value);
	}

	@Override
	public Object convertFromFieldType(IDatabase database, IFieldMetaData field, Class<?> expectedType, Object value)
	{
		if (isBLobColumnName(field.getOriginalTypeName()))
		{
			long oid = conversionHelper.convertValueToType(Number.class, value).longValue();
			try
			{
				PGConnection connection = database.getAutowiredBeanInContext(Connection.class).unwrap(PGConnection.class);
				PostgresBlob blob = new PostgresBlob(connection, oid);
				Object targetValue = null;
				try
				{
					targetValue = conversionHelper.convertValueToType(expectedType, blob);
				}
				finally
				{
					if (targetValue != blob)
					{
						blob.free();
					}
				}
				return targetValue;
			}
			catch (SQLException e)
			{
				throw createPersistenceException(e, null);
			}
		}
		else if (isCLobColumnName(field.getOriginalTypeName()))
		{
			long oid = conversionHelper.convertValueToType(Number.class, value).longValue();
			try
			{
				PGConnection connection = database.getAutowiredBeanInContext(Connection.class).unwrap(PGConnection.class);
				PostgresClob clob = new PostgresClob(connection, oid);

				Object targetValue = null;
				try
				{
					targetValue = conversionHelper.convertValueToType(expectedType, clob);
				}
				finally
				{
					if (targetValue != clob)
					{
						clob.free();
					}
				}
				return targetValue;
			}
			catch (SQLException e)
			{
				throw createPersistenceException(e, null);
			}
		}
		return super.convertFromFieldType(database, field, expectedType, value);
	}

	@Override
	protected ConnectionKeyValue preProcessConnectionIntern(Connection connection, String[] schemaNames, boolean forcePreProcessing) throws SQLException
	{
		Statement stm = null;
		try
		{
			stm = connection.createStatement();
			stm.execute("SET SCHEMA '" + toDefaultCase(schemaNames[0]) + "'");
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(stm);
		}
		return scanForUndeferredDeferrableConstraints(connection, schemaNames);
	}

	@Override
	public void appendIsInOperatorClause(IAppendable appendable)
	{
		appendable.append(" = ANY");
	}

	@Override
	public void appendListClause(List<Object> parameters, IAppendable sb, Class<?> fieldType, IList<Object> splittedIds)
	{
		sb.append(" = ANY (?)");
		IConnectionExtension connectionExtension = serviceContext.getService(IConnectionExtension.class);

		Object javaArray = java.lang.reflect.Array.newInstance(fieldType, splittedIds.size());
		int index = 0;
		for (Object object : splittedIds)
		{
			Object value = conversionHelper.convertValueToType(fieldType, object);
			java.lang.reflect.Array.set(javaArray, index, value);
		}
		Array values = connectionExtension.createJDBCArray(fieldType, javaArray);

		ParamsUtil.addParam(parameters, values);
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
		return BIN_TABLE_NAME.matcher(tableName).matches() || IDX_TABLE_NAME.matcher(tableName).matches();
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
		String sqlState = e.getSQLState();

		if (SQLState.NULL_CONSTRAINT.getXopen().equals(sqlState))
		{
			NullConstraintException ex = new NullConstraintException(e.getMessage(), relatedSql, e);
			ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
			return ex;
		}
		else if (SQLState.UNIQUE_CONSTRAINT.getXopen().equals(sqlState))
		{
			UniqueConstraintException ex = new UniqueConstraintException(e.getMessage(), relatedSql, e);
			ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
			return ex;
		}
		int errorCode = e.getErrorCode();

		if (e.getMessage().contains("" + getOptimisticLockErrorCode()))
		{
			System.out.println("efkwoefwef");
		}

		if (errorCode == getPessimisticLockErrorCode())
		{
			PessimisticLockException ex = new PessimisticLockException(relatedSql, e);
			ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
			return ex;
		}
		if (errorCode == getOptimisticLockErrorCode())
		{
			OptimisticLockException ex = new OptimisticLockException(relatedSql, e);
			ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
			return ex;
		}
		if (errorCode == 1400)
		{
		}

		PersistenceException ex = new PersistenceException(relatedSql, e);
		ex.setStackTrace(e.getStackTrace());

		return ex;
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
		return arrayTypeNameToTypeMap.get(fieldTypeName);
	}

	@Override
	public String getFieldTypeNameByComponentType(Class<?> componentType)
	{
		if (componentType == null)
		{
			return null;
		}
		String[] fieldTypeName = typeToArrayTypeNameMap.get(componentType);
		if (fieldTypeName == null)
		{
			throw new IllegalArgumentException("Can not handle component type '" + componentType + "'");
		}
		return fieldTypeName[0];
	}

	@Override
	public List<String> getAllFullqualifiedSequences(Connection connection, String... schemaNames) throws SQLException
	{
		List<String> allSequenceNames = new ArrayList<String>();

		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT t.sequence_schema || '.' || t.sequence_name FROM information_schema.sequences t WHERE t.sequence_schema"
					+ buildSchemaInClause(schemaNames));
			while (rs.next())
			{
				String fqSequenceName = rs.getString(1);
				allSequenceNames.add(fqSequenceName);
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}

		return allSequenceNames;
	}

	@Override
	public List<String> getAllFullqualifiedTableNames(Connection connection, String... schemaNames) throws SQLException
	{
		List<String> allTableNames = new ArrayList<String>();

		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT t.table_schema || '.' || t.table_name FROM information_schema.tables t WHERE t.table_schema"
					+ buildSchemaInClause(schemaNames));
			while (rs.next())
			{
				String fqTableName = rs.getString(1);
				allTableNames.add(fqTableName);
			}
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}

		return allTableNames;
	}

	@Override
	public List<String> getAllFullqualifiedViews(Connection connection, String... schemaNames) throws SQLException
	{
		List<String> allViewNames = new ArrayList<String>();

		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			for (String schemaName : schemaNames)
			{
				rs = connection.getMetaData().getTables(null, schemaName, null, new String[] { "VIEW" });

				while (rs.next())
				{
					// String schemaName = rs.getString("TABLE_SCHEM");
					String viewName = rs.getString("TABLE_NAME");
					if (!BIN_TABLE_NAME.matcher(viewName).matches() && !IDX_TABLE_NAME.matcher(viewName).matches())
					{
						allViewNames.add(schemaName + "." + viewName);
					}
				}
			}
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}

		return allViewNames;
	}

	@Override
	protected void handleRow(String schemaName, String tableName, String constraintName, de.osthus.ambeth.collections.ArrayList<String> disableConstraintsSQL,
			de.osthus.ambeth.collections.ArrayList<String> enableConstraintsSQL)
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
}
