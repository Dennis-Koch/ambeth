package de.osthus.ambeth.oracle;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.persistence.PessimisticLockException;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.ILoggerHistory;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.log.PersistenceWarnUtil;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.SQLState;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.exception.NullConstraintException;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.connection.IConnectionKeyHandle;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringBuilderUtil;
import de.osthus.ambeth.util.StringConversionHelper;

public class Oracle10gDialect implements IConnectionDialect, IInitializingBean
{
	public static class ConnectionKeyValue
	{

		protected String[] constraintSql;

		protected String[][] disabledSql;

		public ConnectionKeyValue(String[] constraintSql, String[][] disabledSql)
		{
			super();
			this.constraintSql = constraintSql;
			this.disabledSql = disabledSql;
		}

		public String[] getConstraintSql()
		{
			return constraintSql;
		}

		public String[][] getDisabledSql()
		{
			return disabledSql;
		}
	}

	protected static final Pattern BIN_TABLE_NAME = Pattern.compile("BIN\\$.{22}==\\$0", Pattern.CASE_INSENSITIVE);

	protected static final Pattern IDX_TABLE_NAME = Pattern.compile("DR\\$.*?\\$.", Pattern.CASE_INSENSITIVE);

	protected static final LinkedHashMap<Class<?>, String[]> typeToArrayTypeNameMap = new LinkedHashMap<Class<?>, String[]>(128, 0.5f);

	protected static final LinkedHashMap<String, Class<?>> arrayTypeNameToTypeMap = new LinkedHashMap<String, Class<?>>(128, 0.5f);

	protected static final String[] exportedKeysSql = {
			"SELECT CONST.NAME AS CONSTRAINT_NAME, RCONST.NAME AS REF_CONSTRAINT_NAME, OBJ.NAME AS TABLE_NAME, COALESCE(ACOL.NAME, COL.NAME) AS COLUMN_NAME, CCOL.POS# AS POSITION, ROBJ.NAME AS REF_TABLE_NAME, COALESCE(RACOL.NAME, RCOL.NAME) AS REF_COLUMN_NAME, RCCOL.POS# AS REF_POSITION FROM SYS.CON$ CONST INNER JOIN SYS.USER$ USR ON CONST.OWNER# = USR.USER# INNER JOIN SYS.CDEF$ CDEF ON CDEF.CON# = CONST.CON# INNER JOIN SYS.CCOL$ CCOL ON CCOL.CON# = CONST.CON# INNER JOIN SYS.COL$ COL  ON (CCOL.OBJ# = COL.OBJ#) AND (CCOL.INTCOL# = COL.INTCOL#) INNER JOIN SYS.\"_CURRENT_EDITION_OBJ\" OBJ ON CCOL.OBJ# = OBJ.OBJ# LEFT JOIN SYS.ATTRCOL$ ACOL ON (CCOL.OBJ# = ACOL.OBJ#) AND (CCOL.INTCOL# = ACOL.INTCOL#) INNER JOIN SYS.CON$ RCONST ON RCONST.CON# = CDEF.RCON# INNER JOIN SYS.CCOL$ RCCOL ON RCCOL.CON# = RCONST.CON# INNER JOIN SYS.COL$ RCOL  ON (RCCOL.OBJ# = RCOL.OBJ#) AND (RCCOL.INTCOL# = RCOL.INTCOL#) INNER JOIN SYS.\"_CURRENT_EDITION_OBJ\" ROBJ ON RCCOL.OBJ# = ROBJ.OBJ# LEFT JOIN SYS.ATTRCOL$ RACOL  ON (RCCOL.OBJ# = RACOL.OBJ#) AND (RCCOL.INTCOL# = RACOL.INTCOL#) WHERE USR.NAME = ? AND CDEF.TYPE# = 4",
			"SELECT DISTINCT C1.OWNER, C1.CONSTRAINT_NAME, C1.TABLE_NAME AS TABLE_NAME, A1.COLUMN_NAME AS COLUMN_NAME, C2.TABLE_NAME AS REF_TABLE_NAME, A2.COLUMN_NAME AS REF_COLUMN_NAME FROM ALL_CONSTRAINTS C1 JOIN ALL_CONSTRAINTS C2 ON C1.R_CONSTRAINT_NAME = C2.CONSTRAINT_NAME JOIN ALL_CONS_COLUMNS A1 ON C1.CONSTRAINT_NAME = A1.CONSTRAINT_NAME JOIN ALL_CONS_COLUMNS A2 ON C2.CONSTRAINT_NAME = A2.CONSTRAINT_NAME WHERE C1.OWNER = ? AND C1.CONSTRAINT_TYPE = 'R'" };

	static
	{
		typeToArrayTypeNameMap.put(Long.TYPE, new String[] { "LONG_ARRAY", "NUMBER(19,0)" });
		typeToArrayTypeNameMap.put(Long.class, new String[] { "LONG_ARRAY", "NUMBER(19,0)" });
		typeToArrayTypeNameMap.put(Integer.TYPE, new String[] { "INT_ARRAY", "NUMBER(10,0)" });
		typeToArrayTypeNameMap.put(Integer.class, new String[] { "INT_ARRAY", "NUMBER(10,0)" });
		typeToArrayTypeNameMap.put(Short.TYPE, new String[] { "SHORT_ARRAY", "NUMBER(5,0)" });
		typeToArrayTypeNameMap.put(Short.class, new String[] { "SHORT_ARRAY", "NUMBER(5,0)" });
		typeToArrayTypeNameMap.put(Byte.TYPE, new String[] { "BYTE_ARRAY", "NUMBER(3,0)" });
		typeToArrayTypeNameMap.put(Byte.class, new String[] { "BYTE_ARRAY", "NUMBER(3,0)" });
		typeToArrayTypeNameMap.put(Character.TYPE, new String[] { "CHAR_ARRAY", "CHAR" });
		typeToArrayTypeNameMap.put(Character.class, new String[] { "CHAR_ARRAY", "CHAR" });
		typeToArrayTypeNameMap.put(Boolean.TYPE, new String[] { "BOOL_ARRAY", "NUMBER(1,0)" });
		typeToArrayTypeNameMap.put(Boolean.class, new String[] { "BOOL_ARRAY", "NUMBER(1,0)" });
		typeToArrayTypeNameMap.put(Double.TYPE, new String[] { "DOUBLE_ARRAY", "NUMBER(9,9)" });
		typeToArrayTypeNameMap.put(Double.class, new String[] { "DOUBLE_ARRAY", "NUMBER(9,9)" });
		typeToArrayTypeNameMap.put(Float.TYPE, new String[] { "FLOAT_ARRAY", "NUMBER(4,4)" });
		typeToArrayTypeNameMap.put(Float.class, new String[] { "FLOAT_ARRAY", "NUMBER(4,4)" });
		typeToArrayTypeNameMap.put(String.class, new String[] { "STRING_ARRAY", "VARCHAR2(4000 CHAR)" });
		typeToArrayTypeNameMap.put(BigDecimal.class, new String[] { "BIG_DEC_ARRAY", "NUMBER" });
		typeToArrayTypeNameMap.put(BigInteger.class, new String[] { "BIG_INT_ARRAY", "NUMBER(38,0)" });

		// Default behavior. This is an intended "hack" for backwards compatibility.
		typeToArrayTypeNameMap.put(Object.class, new String[] { "BIG_DEC_ARRAY", "NUMBER" });

		for (Entry<Class<?>, String[]> entry : typeToArrayTypeNameMap)
		{
			arrayTypeNameToTypeMap.put(entry.getValue()[0], entry.getKey());
		}
	}

	@LogInstance
	private ILogger log;

	protected final DateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

	protected final WeakHashMap<IConnectionKeyHandle, ConnectionKeyValue> connectionToConstraintSqlMap = new WeakHashMap<IConnectionKeyHandle, ConnectionKeyValue>();

	protected boolean externalTransactionManager;

	protected IConversionHelper conversionHelper;

	protected ILoggerHistory loggerHistory;

	protected ITransactionState transactionState;

	protected boolean autoIndexForeignKeys, autoArrayTypes;

	protected IThreadLocalObjectCollector objectCollector;

	protected final Lock readLock, writeLock;

	public Oracle10gDialect()
	{
		ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		readLock = rwLock.readLock();
		writeLock = rwLock.writeLock();
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
		ParamChecker.assertNotNull(loggerHistory, "loggerHistory");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
	}

	@Property(name = PersistenceConfigurationConstants.ExternalTransactionManager, defaultValue = "false")
	public void setExternalTransactionManager(boolean externalTransactionManager)
	{
		this.externalTransactionManager = externalTransactionManager;
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	public void setLoggerHistory(ILoggerHistory loggerHistory)
	{
		this.loggerHistory = loggerHistory;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setTransactionState(ITransactionState transactionState)
	{
		this.transactionState = transactionState;
	}

	@Property(name = PersistenceConfigurationConstants.AutoIndexForeignKeys, defaultValue = "false")
	public void setAutoIndexForeignKeys(boolean autoIndexForeignKeys)
	{
		this.autoIndexForeignKeys = autoIndexForeignKeys;
	}

	@Property(name = PersistenceConfigurationConstants.AutoArrayTypes, defaultValue = "true")
	public void setAutoArrayTypes(boolean autoArrayTypes)
	{
		this.autoArrayTypes = autoArrayTypes;
	}

	@Override
	public void preProcessConnection(Connection connection, String[] schemaNames, boolean forcePreProcessing)
	{
		try
		{
			ConnectionKeyValue connectionKeyValue = null;
			IConnectionKeyHandle connectionKeyHandle = null;
			Lock writeLock = this.writeLock;

			if (connection.isWrapperFor(IConnectionKeyHandle.class))
			{
				connectionKeyHandle = connection.unwrap(IConnectionKeyHandle.class);
				writeLock.lock();
				try
				{
					// WeakHashMaps have ALWAYS to be exclusively locked even if they SEEM to be only read-accessed
					connectionKeyValue = connectionToConstraintSqlMap.get(connectionKeyHandle);
				}
				finally
				{
					writeLock.unlock();
				}
			}
			if (forcePreProcessing || connectionKeyValue == null)
			{
				if (connectionKeyHandle == null)
				{
					throw new IllegalStateException("Should never happen");
				}
				handleIndices(connection);

				if (autoArrayTypes)
				{
					handleArrayTypes(connection);
				}

				connectionKeyValue = handleForeignKeyConstraints(connection, schemaNames);
				writeLock.lock();
				try
				{
					connectionToConstraintSqlMap.put(connectionKeyHandle, connectionKeyValue);
				}
				finally
				{
					writeLock.unlock();
				}
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	private ConnectionKeyValue handleForeignKeyConstraints(Connection connection, String[] schemaNames) throws SQLException
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		StringBuilder sql = tlObjectCollector.create(StringBuilder.class);
		StringBuilder fullNameSB = tlObjectCollector.create(StringBuilder.class);
		ArrayList<String> constraintSqlList = new ArrayList<String>();
		ArrayList<String[]> disabled = new ArrayList<String[]>();
		String schemaIn = StringConversionHelper.implode(objectCollector, schemaNames, "', '");
		Statement stm = connection.createStatement();
		try
		{
			stm.execute("SELECT OWNER, TABLE_NAME, CONSTRAINT_NAME FROM ALL_CONSTRAINTS WHERE OWNER IN ('" + schemaIn
					+ "') AND STATUS = 'ENABLED' AND DEFERRABLE = 'DEFERRABLE' AND DEFERRED = 'IMMEDIATE' AND CONSTRAINT_TYPE = 'R'");
			ResultSet rs = stm.getResultSet();
			while (rs.next())
			{
				String schemaName = rs.getString("OWNER");
				String tableName = rs.getString("TABLE_NAME");
				String constraintName = rs.getString("CONSTRAINT_NAME");
				if (!BIN_TABLE_NAME.matcher(tableName).matches())
				{
					fullNameSB.setLength(0);
					fullNameSB.append(schemaName).append(".").append(constraintName);
					String fullName = fullNameSB.toString();
					sql.setLength(0);
					sql.append("SET CONSTRAINT ").append(fullName).append(" DEFERRED");
					constraintSqlList.add(sql.toString());
					String[] toRemember = new String[2];
					toRemember[1] = fullName;
					fullNameSB.setLength(0);
					fullNameSB.append(schemaName).append(".").append(tableName);
					toRemember[0] = fullNameSB.toString();
					disabled.add(toRemember);
				}
			}

			String[] constraintSqlArray = constraintSqlList.toArray(new String[constraintSqlList.size()]);
			String[][] disabledArray = disabled.toArray(new String[disabled.size()][]);
			ConnectionKeyValue connectionKeyValue = new ConnectionKeyValue(constraintSqlArray, disabledArray);

			return connectionKeyValue;
		}
		finally
		{
			JdbcUtil.close(stm);
		}
	}

	private void handleArrayTypes(Connection connection) throws SQLException
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		Statement stm = connection.createStatement();
		ResultSet rs = null;
		try
		{
			rs = stm.executeQuery("SELECT object_type, object_name FROM user_objects WHERE object_type IN ('TYPE')");

			LinkedHashMap<String, Class<?>> nameToArrayTypeMap = new LinkedHashMap<String, Class<?>>();
			for (Entry<Class<?>, String[]> entry : typeToArrayTypeNameMap)
			{
				Class<?> type = entry.getKey();
				nameToArrayTypeMap.put(entry.getValue()[0], type);
			}

			while (rs.next())
			{
				String typeName = rs.getString("object_name");
				nameToArrayTypeMap.remove(typeName);
			}
			JdbcUtil.close(rs);
			rs = null;
			for (Entry<String, Class<?>> entry : nameToArrayTypeMap)
			{
				String necessaryTypeName = entry.getKey();

				StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
				try
				{
					sb.append("CREATE TYPE ").append(necessaryTypeName).append(" AS VARRAY(4000) OF ");
					String arrayTypeName = typeToArrayTypeNameMap.get(entry.getValue())[1];
					sb.append(arrayTypeName);
					stm.execute(sb.toString());
				}
				finally
				{
					tlObjectCollector.dispose(sb);
				}
			}
		}
		finally
		{
			JdbcUtil.close(stm, rs);
		}
	}

	@Override
	public IList<IMap<String, String>> getExportedKeys(Connection connection, String schemaName) throws SQLException
	{
		ArrayList<IMap<String, String>> allForeignKeys = new ArrayList<IMap<String, String>>();
		PreparedStatement pstm = null;
		ResultSet allForeignKeysRS = null;
		try
		{
			for (int a = 0, size = exportedKeysSql.length; a < size; a++)
			{
				String sql = exportedKeysSql[a];
				try
				{
					pstm = connection.prepareStatement(sql);
					pstm.setString(1, schemaName);
					allForeignKeysRS = pstm.executeQuery();
					break;
				}
				catch (PersistenceException e)
				{
					JdbcUtil.close(pstm, allForeignKeysRS);
					if (e.getCause() instanceof SQLException)
					{
						if (SQLState.ACCESS_VIOLATION.getXopen().equals(((SQLException) e.getCause()).getSQLState()))
						{
							continue;
						}
					}
					throw e;
				}
				catch (SQLException e)
				{
					JdbcUtil.close(pstm, allForeignKeysRS);
					if (SQLState.ACCESS_VIOLATION.getXopen().equals(((SQLException) e.getCause()).getSQLState()))
					{
						continue;
					}
					throw e;
				}
			}
			if (allForeignKeysRS != null)
			{
				while (allForeignKeysRS.next())
				{
					HashMap<String, String> foreignKey = new HashMap<String, String>();

					foreignKey.put("OWNER", schemaName);
					foreignKey.put("CONSTRAINT_NAME", allForeignKeysRS.getString("CONSTRAINT_NAME").toUpperCase());
					foreignKey.put("FKTABLE_NAME", allForeignKeysRS.getString("TABLE_NAME").toUpperCase());
					foreignKey.put("FKCOLUMN_NAME", allForeignKeysRS.getString("COLUMN_NAME").toUpperCase());
					foreignKey.put("PKTABLE_NAME", allForeignKeysRS.getString("REF_TABLE_NAME").toUpperCase());
					foreignKey.put("PKCOLUMN_NAME", allForeignKeysRS.getString("REF_COLUMN_NAME").toUpperCase());

					allForeignKeys.add(foreignKey);
				}
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
		Statement stmt = connection.createStatement();
		ResultSet fulltextIndexesRS = null;
		try
		{
			fulltextIndexesRS = stmt
					.executeQuery("SELECT A.TABLE_NAME, A.COLUMN_NAME FROM ALL_IND_COLUMNS A JOIN ALL_INDEXES B ON A.INDEX_NAME = B.INDEX_NAME AND A.TABLE_NAME = B.TABLE_NAME WHERE A.INDEX_OWNER = '"
							+ schemaName + "' AND B.INDEX_TYPE = 'DOMAIN' AND B.ITYP_OWNER = 'CTXSYS' AND B.ITYP_NAME = 'CONTEXT'");
			while (fulltextIndexesRS.next())
			{
				String tableName = fulltextIndexesRS.getString("TABLE_NAME").toUpperCase();
				String columnName = fulltextIndexesRS.getString("COLUMN_NAME").toUpperCase();

				IList<String> fulltextColumns = fulltextIndexes.get(tableName);
				if (fulltextColumns == null)
				{
					fulltextColumns = new ArrayList<String>();
					fulltextIndexes.put(tableName, fulltextColumns);
				}
				fulltextColumns.add(columnName);
			}
		}
		finally
		{
			JdbcUtil.close(stmt, fulltextIndexesRS);
		}

		return fulltextIndexes;
	}

	@Override
	public boolean isSystemTable(String tableName)
	{
		return BIN_TABLE_NAME.matcher(tableName).matches() || IDX_TABLE_NAME.matcher(tableName).matches();
	}

	@Override
	public boolean handleField(Class<?> fieldType, Object value, StringBuilder targetSb) throws Throwable
	{
		if (fieldType.equals(Date.class) || fieldType.equals(java.sql.Date.class))
		{
			Date date = conversionHelper.convertValueToType(Date.class, value);
			targetSb.append("TO_DATE('");
			targetSb.append(defaultDateFormat.format(date));
			targetSb.append("','YYYY-MM-DD HH24-MI-SS')");
			return true;
		}
		return false;
	}

	@Override
	public IList<String[]> disableConstraints(Connection connection)
	{
		ArrayList<String[]> disabled = new ArrayList<String[]>();

		try
		{
			ConnectionKeyValue connectionKeyValue;
			IConnectionKeyHandle connectionKeyHandle = null;

			if (connection.isWrapperFor(IConnectionKeyHandle.class))
			{
				connectionKeyHandle = connection.unwrap(IConnectionKeyHandle.class);
				Lock writeLock = this.writeLock;
				writeLock.lock();
				try
				{
					// WeakHashMaps have ALWAYS to be exclusively locked even if they SEEM to be only read-accessed
					connectionKeyValue = connectionToConstraintSqlMap.get(connectionKeyHandle);
				}
				finally
				{
					writeLock.unlock();
				}
			}
			else
			{
				throw new IllegalStateException("Connection is not a wrapper for " + IConnectionKeyHandle.class.getName());
			}
			String[][] disabledSql = connectionKeyValue.getDisabledSql();
			disabled.addAll(disabledSql);

			String[] constraintSql = connectionKeyValue.getConstraintSql();

			if (constraintSql.length > 0)
			{
				Statement stm = connection.createStatement();
				try
				{
					for (int a = 0, size = constraintSql.length; a < size; a++)
					{
						stm.addBatch(constraintSql[a]);
					}
					stm.executeBatch();
				}
				finally
				{
					JdbcUtil.close(stm);
				}
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}

		return disabled;
	}

	@Override
	public void enableConstraints(Connection connection, IList<String[]> disabled)
	{
		if (disabled == null || disabled.isEmpty())
		{
			return;
		}
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		Statement stmt = null;
		StringBuilder sql = null;
		try
		{
			sql = tlObjectCollector.create(StringBuilder.class);
			stmt = connection.createStatement();
			for (int i = disabled.size(); i-- > 0;)
			{
				sql.append("SET CONSTRAINT ").append(disabled.get(i)[1]).append(" IMMEDIATE");
				stmt.addBatch(sql.toString());
				sql.setLength(0);
			}
			stmt.executeBatch();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			tlObjectCollector.dispose(sql);
			JdbcUtil.close(stmt);
		}
	}

	@Override
	public void commit(Connection connection) throws SQLException
	{
		Boolean active = transactionState != null ? transactionState.isExternalTransactionManagerActive() : null;
		if (active == null)
		{
			active = Boolean.valueOf(externalTransactionManager);
		}
		if (active.booleanValue())
		{
			// No Action!
			// Transactions are externally managed.
		}
		else
		{
			connection.commit();
		}
	}

	@Override
	public void rollback(Connection connection) throws SQLException
	{
		Boolean active = transactionState != null ? transactionState.isExternalTransactionManagerActive() : null;
		if (active == null)
		{
			active = Boolean.valueOf(externalTransactionManager);
		}
		if (active.booleanValue())
		{
			// No Action!
			// Transactions are externally managed.
		}
		else
		{
			connection.rollback();
		}
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint, Connection connection) throws SQLException
	{
		// noop: releaseSavepoint(Savepoint savepoint) is not supported by Oracle10g
	}

	@Override
	public int getOptimisticLockErrorCode()
	{
		return 20800;
	}

	@Override
	public int getResourceBusyErrorCode()
	{
		return 54;
	}

	@Override
	public PersistenceException createPersistenceException(SQLException e, String relatedSql)
	{
		int errorCode = e.getErrorCode();

		// 54 = RESOURCE BUSY acquiring with NOWAIT (pessimistic lock)
		if (errorCode == 54)
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
			NullConstraintException ex = new NullConstraintException(e.getMessage(), relatedSql, e);
			ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
			return ex;
		}
		return null;
	}

	@Override
	public boolean useVersionOnOptimisticUpdate()
	{
		return false;
	}

	@Override
	public ResultSet getIndexInfo(Connection connection, String schemaName, String tableName, boolean unique) throws SQLException
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sql = tlObjectCollector.create(StringBuilder.class);
		try
		{
			sql.append("SELECT AI.TABLE_NAME, AI.INDEX_NAME, INDEX_TYPE AS TYPE, COLUMN_POSITION AS ORDINAL_POSITION, COLUMN_NAME FROM ALL_INDEXES AI JOIN ALL_IND_COLUMNS AIC ON AI.INDEX_NAME = AIC.INDEX_NAME WHERE AI.OWNER = ? AND AI.TABLE_NAME = ?");
			if (unique)
			{
				sql.append(" AND AI.UNIQUENESS = 'UNIQUE'");
			}
			PreparedStatement prep = connection.prepareStatement(sql.toString());
			prep.setString(1, schemaName);
			prep.setString(2, tableName);
			return prep.executeQuery();
		}
		finally
		{
			tlObjectCollector.dispose(sql);
		}
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

	protected void handleIndices(Connection connection) throws SQLException
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		Statement stm = connection.createStatement();
		Statement createIndexStm = null;
		ResultSet rs = null;
		try
		{
			rs = stm.executeQuery("select table_name, constraint_name,cname1"
					+ "|| nvl2(cname2,','||cname2,null) || nvl2(cname3,','||cname3,null) || nvl2(cname4,','||cname4,null)"
					+ "|| nvl2(cname5,','||cname5,null) || nvl2(cname6,','||cname6,null) || nvl2(cname7,','||cname7,null) || nvl2(cname8,','||cname8,null)"
					+ " columns" + " from ( select b.table_name," + " b.constraint_name," + " max(decode( position, 1, column_name, null )) cname1,"
					+ " max(decode( position, 2, column_name, null )) cname2, max(decode( position, 3, column_name, null )) cname3,"
					+ " max(decode( position, 4, column_name, null )) cname4, max(decode( position, 5, column_name, null )) cname5,"
					+ " max(decode( position, 6, column_name, null )) cname6, max(decode( position, 7, column_name, null )) cname7,"
					+ " max(decode( position, 8, column_name, null )) cname8, count(*) col_cnt from (select substr(table_name,1,30) table_name,"
					+ " substr(constraint_name,1,30) constraint_name, substr(column_name,1,30) column_name, position from user_cons_columns ) a,"
					+ " user_constraints b where a.constraint_name = b.constraint_name and b.constraint_type = 'R'"
					+ " group by b.table_name, b.constraint_name ) cons where col_cnt > ALL ( select count(*) from user_ind_columns i"
					+ " where i.table_name = cons.table_name and i.column_name in (cname1, cname2, cname3, cname4, cname5, cname6, cname7, cname8 )"
					+ " and i.column_position <= cons.col_cnt group by i.index_name )");

			int maxIndexLength = -1;
			boolean constraintFound = false;
			while (rs.next())
			{
				constraintFound = true;
				String tableName = rs.getString("table_name");
				String constraintName = rs.getString("constraint_name");
				String columns = rs.getString("columns");

				if (autoIndexForeignKeys)
				{
					if (createIndexStm == null)
					{
						createIndexStm = connection.createStatement();
						maxIndexLength = connection.getMetaData().getMaxTableNameLength();
					}
					String indexName = StringBuilderUtil.concat(tlObjectCollector, "IX_", constraintName);
					if (indexName.length() > maxIndexLength)
					{
						// Index has to be truncated and randomized to 'ensure' uniqueness
						indexName = indexName.substring(0, maxIndexLength - 2) + (int) (Math.random() * 98 + 1);
					}
					String sql = StringBuilderUtil.concat(tlObjectCollector, "CREATE INDEX \"", indexName, "\" ON \"", tableName, "\" (\"", columns, "\")");
					createIndexStm.addBatch(sql);
				}
				else
				{
					if (log.isWarnEnabled())
					{
						PersistenceWarnUtil.logWarnOnce(log, loggerHistory, connection, "No index for constraint '" + constraintName + "' on table '"
								+ tableName + "' for column '" + columns + "' found");
					}
				}
			}
			if (createIndexStm != null)
			{
				createIndexStm.executeBatch();
				if (log.isDebugEnabled())
				{
					log.debug("Runtime creation of indexes for foreign key constraints successful. This has been done because '"
							+ PersistenceConfigurationConstants.AutoIndexForeignKeys + "' has been specified to 'true'");
				}
			}
			if (constraintFound && !autoIndexForeignKeys)
			{
				if (log.isWarnEnabled())
				{
					PersistenceWarnUtil.logWarnOnce(log, loggerHistory, connection,
							"At least one missing index found on foreign key constraints. Maybe you should specify '"
									+ PersistenceConfigurationConstants.AutoIndexForeignKeys
									+ "=true' to allow auto-indexing at runtime or review your database schema");
				}
			}
		}
		finally
		{
			JdbcUtil.close(createIndexStm);
			JdbcUtil.close(stm, rs);
		}
	}
}
