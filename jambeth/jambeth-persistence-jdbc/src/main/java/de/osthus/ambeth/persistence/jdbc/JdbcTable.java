package de.osthus.ambeth.persistence.jdbc;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.sql.CompositeResultSet;
import de.osthus.ambeth.sql.IResultSet;
import de.osthus.ambeth.sql.IResultSetProvider;
import de.osthus.ambeth.sql.SqlTable;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IParamHolder;
import de.osthus.ambeth.util.OptimisticLockUtil;
import de.osthus.ambeth.util.ParamChecker;

public class JdbcTable extends SqlTable
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected Connection connection;

	protected boolean batching = false;

	protected LinkedHashMap<Object, Object> persistedIdToVersionMap = new LinkedHashMap<Object, Object>();

	protected LinkedHashMap<Integer, ILinkedMap<String, PreparedStatement>> fieldsToInsertStmtMap = new LinkedHashMap<Integer, ILinkedMap<String, PreparedStatement>>();

	protected LinkedHashMap<Integer, ILinkedMap<String, PreparedStatement>> fieldsToUpdateStmtMap = new LinkedHashMap<Integer, ILinkedMap<String, PreparedStatement>>();

	protected PreparedStatement deleteStmt;

	@Property(name = MergeConfigurationConstants.ExactVersionForOptimisticLockingRequired, defaultValue = "false")
	protected boolean exactVersionForOptimisticLockingRequired;

	protected int maxInClauseBatchThreshold;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		maxInClauseBatchThreshold = connectionDialect.getMaxInClauseBatchThreshold();
	}

	@Override
	public void startBatch()
	{
		if (batching)
		{
			throw new IllegalStateException("Batch queuing already started");
		}
		batching = true;
	}

	// TODO Finding an useful return value
	@Override
	public int[] finishBatch()
	{
		if (!batching)
		{
			throw new IllegalStateException("No batch queue open");
		}
		try
		{
			checkRowLocks(persistedIdToVersionMap);

			if (deleteStmt != null)
			{
				deleteStmt.executeBatch();
				deleteStmt.close();
				deleteStmt = null;
			}
			executeBatchedStatements(fieldsToUpdateStmtMap);
			executeBatchedStatements(fieldsToInsertStmtMap);
			return new int[0];
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			clearBatch();
		}
	}

	@Override
	public void clearBatch()
	{
		try
		{
			fieldsToInsertStmtMap.clear();
			fieldsToUpdateStmtMap.clear();
			persistedIdToVersionMap.clear();
			if (deleteStmt != null)
			{
				deleteStmt.close();
				deleteStmt = null;
			}
			batching = false;
		}
		catch (SQLException e)
		{
			// Intended blank
		}
	}

	protected void executeBatchedStatements(ILinkedMap<Integer, ILinkedMap<String, PreparedStatement>> fieldsToStmtMap)
	{
		if (fieldsToStmtMap.size() == 0)
		{
			return;
		}
		try
		{
			Iterator<Entry<Integer, ILinkedMap<String, PreparedStatement>>> iter = fieldsToStmtMap.iterator();
			while (iter.hasNext())
			{
				Entry<Integer, ILinkedMap<String, PreparedStatement>> entry = iter.next();
				for (Entry<String, PreparedStatement> perNamesEntry : entry.getValue())
				{
					PreparedStatement prep = perNamesEntry.getValue();
					prep.executeBatch();
				}
			}
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			Iterator<Entry<Integer, ILinkedMap<String, PreparedStatement>>> iter = fieldsToStmtMap.iterator();
			while (iter.hasNext())
			{
				Entry<Integer, ILinkedMap<String, PreparedStatement>> entry = iter.next();
				for (Entry<String, PreparedStatement> perNamesEntry : entry.getValue())
				{
					PreparedStatement prep = perNamesEntry.getValue();
					try
					{
						prep.close();
					}
					catch (SQLException e)
					{
						// Intended blank
					}
				}
			}
			fieldsToStmtMap.clear();
		}
	}

	@Override
	public void delete(List<IObjRef> oris)
	{
		if (oris == null || oris.size() == 0)
		{
			return;
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		Class<?> entityType = getEntityType();
		Class<?> idFieldType = getIdField().getFieldType();
		Class<?> versionFieldType = getVersionField() != null ? getVersionField().getFieldType() : null;
		PreparedStatement prep = createDeleteStatementWithIn();
		try
		{
			for (int a = 0, size = oris.size(); a < size; a++)
			{
				IObjRef ori = oris.get(a);
				if (!entityType.equals(ori.getRealType()))
				{
					throw new IllegalArgumentException("ORI invalid");
				}
				Object persistenceId = conversionHelper.convertValueToType(idFieldType, ori.getId());
				Object persistenceVersion = null;
				if (versionFieldType != null)
				{
					persistenceVersion = conversionHelper.convertValueToType(versionFieldType, ori.getVersion());
				}
				persistedIdToVersionMap.put(persistenceId, persistenceVersion);
				prep.setObject(1, persistenceId);
				prep.addBatch();
			}
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			try
			{
				prep.clearParameters();
			}
			catch (Throwable e)
			{
				// Intended blank
			}
		}
	}

	protected ILinkedMap<String, PreparedStatement> getEnsureMap(int id, LinkedHashMap<Integer, ILinkedMap<String, PreparedStatement>> map)
	{
		Integer value = Integer.valueOf(id);
		ILinkedMap<String, PreparedStatement> perCount = map.get(value);
		if (perCount == null)
		{
			perCount = new LinkedHashMap<String, PreparedStatement>();
			map.put(value, perCount);
		}
		return perCount;
	}

	@Override
	public Object insert(Object id, IParamHolder<Object> newId, ILinkedMap<String, Object> puis)
	{
		ParamChecker.assertParamNotNull(id, "id");
		ParamChecker.assertParamNotNull(newId, "newId");
		ParamChecker.assertTrue(batching, "batching");

		String[] fieldNames = new String[getAllFields().size()];
		Object[] values = new Object[fieldNames.length];
		String namesKey = generateNamesKey(puis, fieldNames, values);

		ILinkedMap<String, PreparedStatement> perCount = getEnsureMap(puis.size(), fieldsToInsertStmtMap);
		PreparedStatement prep = perCount.get(namesKey);
		if (prep == null)
		{
			prep = createInsertStatement(fieldNames);
			perCount.put(namesKey, prep);
		}

		IField versionField = this.versionField;
		Object initialVersion = this.initialVersion;
		newId.setValue(conversionHelper.convertValueToType(idField.getMember().getRealType(), id));
		Object newVersion = versionField != null ? conversionHelper.convertValueToType(versionField.getMember().getRealType(), initialVersion) : null;

		try
		{
			int index = 1;
			prep.setObject(index++, conversionHelper.convertValueToType(idField.getFieldType(), id));
			if (versionField != null)
			{
				prep.setObject(index++, conversionHelper.convertValueToType(versionField.getFieldType(), initialVersion));
			}
			for (int a = 0, size = fieldNames.length; a < size; a++)
			{
				String fieldName = fieldNames[a];
				if (fieldName == null)
				{
					// Value not specified
					continue;
				}
				Object convertedValue = values[a];
				prep.setObject(index++, convertedValue);
			}
			IField createdOnField = getCreatedOnField();
			if (createdOnField != null)
			{
				Object convertedValue = conversionHelper.convertValueToType(createdOnField.getFieldType(), contextProvider.getCurrentTime());
				prep.setObject(index++, convertedValue);
			}
			IField createdByField = getCreatedByField();
			if (createdByField != null)
			{
				Object convertedValue = conversionHelper.convertValueToType(createdByField.getFieldType(), contextProvider.getCurrentUser());
				prep.setObject(index++, convertedValue);
			}

			prep.addBatch();
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			try
			{
				prep.clearParameters();
			}
			catch (Throwable e)
			{
				// Intended blank
			}
		}

		return newVersion;
	}

	@Override
	public Object update(Object id, Object version, ILinkedMap<String, Object> puis)
	{
		ParamChecker.assertParamNotNull(id, "id");
		IField versionField = getVersionField();
		if (versionField != null)
		{
			ParamChecker.assertParamNotNull(version, "version");
		}
		ParamChecker.assertTrue(batching, "batching");
		IConversionHelper conversionHelper = this.conversionHelper;

		String[] fieldNames = new String[getAllFields().size()];
		Object[] values = new Object[fieldNames.length];
		String namesKey = generateNamesKey(puis, fieldNames, values);

		ILinkedMap<String, PreparedStatement> perCount = getEnsureMap(puis.size(), fieldsToUpdateStmtMap);
		PreparedStatement prep = perCount.get(namesKey);
		if (prep == null)
		{
			prep = createUpdateStatement(fieldNames);
			perCount.put(namesKey, prep);
		}

		Object newVersion = null;
		if (versionField != null)
		{
			Class<?> versionFieldType = versionField.getFieldType();
			version = conversionHelper.convertValueToType(versionFieldType, version);

			if (versionFieldType.equals(Long.class))
			{
				newVersion = Long.valueOf((Long) version + 1);
			}
			else if (versionFieldType.equals(Integer.class))
			{
				newVersion = Integer.valueOf((Integer) version + 1);
			}
			else if (versionFieldType.equals(Short.class))
			{
				newVersion = Short.valueOf((short) ((Short) version + 1));
			}
			else if (versionFieldType.equals(Byte.class))
			{
				newVersion = Byte.valueOf((byte) ((Byte) version + 1));
			}
			else if (versionFieldType.equals(Date.class))
			{
				newVersion = new Date(contextProvider.getCurrentTime());
			}
			else if (versionFieldType.equals(BigInteger.class))
			{
				newVersion = ((BigInteger) version).add(BigInteger.ONE);
			}
			else if (versionFieldType.equals(BigDecimal.class))
			{
				newVersion = ((BigDecimal) version).add(BigDecimal.ONE);
			}
			else
			{
				throw new IllegalArgumentException("Version type not supported: " + version.getClass());
			}
		}

		try
		{
			int index = 1;
			if (versionField != null)
			{
				prep.setObject(index++, conversionHelper.convertValueToType(versionField.getFieldType(), newVersion));
			}
			for (int a = 0, size = fieldNames.length; a < size; a++)
			{
				String fieldName = fieldNames[a];
				if (fieldName == null)
				{
					// Value not specified
					continue;
				}
				Object convertedValue = values[a];
				prep.setObject(index++, convertedValue);
			}
			IField updatedOnField = getUpdatedOnField();
			if (updatedOnField != null)
			{
				Object convertedValue = conversionHelper.convertValueToType(updatedOnField.getFieldType(), contextProvider.getCurrentTime());
				prep.setObject(index++, convertedValue);
			}
			IField updatedByField = getUpdatedByField();
			if (updatedByField != null)
			{
				Object convertedValue = conversionHelper.convertValueToType(updatedByField.getFieldType(), contextProvider.getCurrentUser());
				prep.setObject(index++, convertedValue);
			}

			Object persistenceId = conversionHelper.convertValueToType(idField.getFieldType(), id);
			Object persistenceVersion = null;
			prep.setObject(index++, persistenceId);
			if (versionField != null)
			{
				persistenceVersion = conversionHelper.convertValueToType(versionField.getFieldType(), version);
			}
			if (connectionDialect.useVersionOnOptimisticUpdate())
			{
				prep.setObject(index++, persistenceVersion);
			}
			prep.addBatch();

			persistedIdToVersionMap.put(persistenceId, persistenceVersion);
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			try
			{
				prep.clearParameters();
			}
			catch (Throwable e)
			{
				// Intended blank
			}
		}
		return newVersion;
	}

	@SuppressWarnings("unchecked")
	protected void checkRowLocks(ILinkedMap<Object, Object> persistedIdToVersionMap) throws SQLException
	{
		if (persistedIdToVersionMap.size() == 0)
		{
			return;
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		boolean exactVersionForOptimisticLockingRequired = this.exactVersionForOptimisticLockingRequired;
		Class<?> idFieldType = getIdField().getFieldType();
		ArrayList<Object> persistedIdsForArray = new ArrayList<Object>();

		persistedIdToVersionMap.toKeysList(persistedIdsForArray);

		Class<?> versionFieldType = getVersionField() != null ? getVersionField().getFieldType() : null;
		IResultSet selectForUpdateRS = createSelectForUpdateStatementWithIn(persistedIdsForArray);
		try
		{
			while (selectForUpdateRS.moveNext())
			{
				Object[] current = selectForUpdateRS.getCurrent();
				Object persistedId = conversionHelper.convertValueToType(idFieldType, current[0]);
				Object givenPersistedVersion = persistedIdToVersionMap.remove(persistedId);
				if (versionFieldType == null)
				{
					continue;
				}
				Object persistedVersion = conversionHelper.convertValueToType(versionFieldType, current[1]);
				if (log.isDebugEnabled())
				{
					log.debug("Given: " + getName() + " - " + persistedId + ", Version: " + givenPersistedVersion + ", VersionInDb: " + persistedVersion);
				}

				if (persistedVersion == null)
				{
					continue;
				}
				if (exactVersionForOptimisticLockingRequired)
				{
					if (!persistedVersion.equals(givenPersistedVersion))
					{
						Object objId = conversionHelper.convertValueToType(getIdField().getMember().getRealType(), persistedId);
						Object objVersion = conversionHelper.convertValueToType(getVersionField().getMember().getRealType(), persistedVersion);
						throw OptimisticLockUtil.throwModified(new ObjRef(getEntityType(), objId, objVersion), givenPersistedVersion);
					}
				}
				else
				{
					if (((Comparable<Object>) persistedVersion).compareTo(givenPersistedVersion) > 0)
					{
						Object objId = conversionHelper.convertValueToType(getIdField().getMember().getRealType(), persistedId);
						Object objVersion = conversionHelper.convertValueToType(getVersionField().getMember().getRealType(), persistedVersion);
						throw OptimisticLockUtil.throwModified(new ObjRef(getEntityType(), objId, objVersion), givenPersistedVersion);
					}
				}
			}
			if (persistedIdToVersionMap.size() > 0)
			{
				Object objId = conversionHelper.convertValueToType(getIdField().getMember().getRealType(), persistedIdToVersionMap.iterator().next().getKey());
				throw OptimisticLockUtil.throwDeleted(new ObjRef(getEntityType(), objId, null));
			}
		}
		finally
		{
			selectForUpdateRS.dispose();
		}
	}

	protected PreparedStatement createInsertStatement(String[] fieldNames)
	{
		IField idField = getIdField();
		IField versionField = getVersionField();

		int variableCount = 0;
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sqlSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			sqlSB.append("INSERT INTO ");
			sqlBuilder.appendName(getName(), sqlSB).append(" (");
			sqlBuilder.appendName(idField.getName(), sqlSB);
			variableCount++;
			if (versionField != null)
			{
				sqlSB.append(',');
				sqlBuilder.appendName(versionField.getName(), sqlSB);
				variableCount++;
			}

			for (int a = 0, size = fieldNames.length; a < size; a++)
			{
				String fieldName = fieldNames[a];
				if (fieldName == null)
				{
					// Value not specified
					continue;
				}
				sqlSB.append(',');
				sqlBuilder.appendName(fieldName, sqlSB);
				variableCount++;
			}
			IField createdOnField = getCreatedOnField();
			if (createdOnField != null)
			{
				sqlSB.append(',');
				sqlBuilder.appendName(createdOnField.getName(), sqlSB);
				variableCount++;
			}
			IField createdByField = getCreatedByField();
			if (createdByField != null)
			{
				sqlSB.append(',');
				sqlBuilder.appendName(createdByField.getName(), sqlSB);
				variableCount++;
			}
			sqlSB.append(") VALUES (");
			boolean first = true;
			for (int a = variableCount; a-- > 0;)
			{
				if (first)
				{
					first = false;
					sqlSB.append("?");
				}
				else
				{
					sqlSB.append(",?");
				}
			}
			sqlSB.append(')');

			if (log.isDebugEnabled())
			{
				log.debug("prepare: " + sqlSB.toString());
			}

			PreparedStatement pstm = connection.prepareStatement(sqlSB.toString());
			pstm.setQueryTimeout(30);
			return pstm;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, sqlSB.toString());
		}
		finally
		{
			tlObjectCollector.dispose(sqlSB);
		}
	}

	protected IResultSet createSelectForUpdateStatementWithIn(List<Object> ids)
	{
		if (ids.size() <= maxInClauseBatchThreshold)
		{
			return createSelectForUpdateStatementWithInIntern(ids);
		}
		IList<IList<Object>> splitValues = persistenceHelper.splitValues(ids, maxInClauseBatchThreshold);

		ArrayList<IResultSetProvider> resultSetProviderStack = new ArrayList<IResultSetProvider>(splitValues.size());
		// Stack gets evaluated last->first so back iteration is correct to execute the sql in order later
		for (int a = splitValues.size(); a-- > 0;)
		{
			final IList<Object> values = splitValues.get(a);
			resultSetProviderStack.add(new IResultSetProvider()
			{
				@Override
				public void skipResultSet()
				{
					// Intended blank
				}

				@Override
				public IResultSet getResultSet()
				{
					return createSelectForUpdateStatementWithInIntern(values);
				}
			});
		}
		CompositeResultSet compositeResultSet = new CompositeResultSet();
		compositeResultSet.setResultSetProviderStack(resultSetProviderStack);
		compositeResultSet.afterPropertiesSet();
		return compositeResultSet;
	}

	protected IResultSet createSelectForUpdateStatementWithInIntern(List<Object> ids)
	{
		IField idField = getIdField();
		IField versionField = getVersionField();
		LinkedHashMap<Integer, Object> params = new LinkedHashMap<Integer, Object>();
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder fieldNamesSQL = tlObjectCollector.create(StringBuilder.class);
		StringBuilder whereSQL = tlObjectCollector.create(StringBuilder.class);
		try
		{
			sqlBuilder.appendName(idField.getName(), fieldNamesSQL);
			if (versionField != null)
			{
				fieldNamesSQL.append(',');
				sqlBuilder.appendName(versionField.getName(), fieldNamesSQL);
			}
			persistenceHelper.appendSplittedValues(idField.getName(), idField.getFieldType(), ids, params, whereSQL);
			whereSQL.append(" FOR UPDATE NOWAIT");

			return sqlConnection.selectFields(getName(), fieldNamesSQL, whereSQL, params);
		}
		finally
		{
			tlObjectCollector.dispose(whereSQL);
			tlObjectCollector.dispose(fieldNamesSQL);
		}
	}

	protected PreparedStatement createUpdateStatement(String[] fieldNames)
	{
		IField idField = getIdField();
		IField versionField = getVersionField();

		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sqlSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			sqlSB.append("UPDATE ");
			sqlBuilder.appendName(getName(), sqlSB);
			sqlSB.append(" SET ");

			boolean firstField = true;
			if (versionField != null)
			{
				firstField = false;
				sqlBuilder.appendName(versionField.getName(), sqlSB).append("=?");
			}

			for (int a = 0, size = fieldNames.length; a < size; a++)
			{
				String fieldName = fieldNames[a];
				if (fieldName == null)
				{
					// Value not specified
					continue;
				}
				if (!firstField)
				{
					sqlSB.append(',');
				}
				firstField = false;
				sqlBuilder.appendName(fieldName, sqlSB).append("=?");
			}
			IField updatedOnField = getUpdatedOnField();
			if (updatedOnField != null)
			{
				if (!firstField)
				{
					sqlSB.append(',');
				}
				firstField = false;
				sqlBuilder.appendName(updatedOnField.getName(), sqlSB).append("=?");
			}
			IField updatedByField = getUpdatedByField();
			if (updatedByField != null)
			{
				if (!firstField)
				{
					sqlSB.append(',');
				}
				firstField = false;
				sqlBuilder.appendName(updatedByField.getName(), sqlSB).append("=?");
			}
			sqlSB.append(" WHERE ");
			sqlBuilder.appendName(idField.getName(), sqlSB).append("=?");
			if (connectionDialect.useVersionOnOptimisticUpdate() && versionField != null)
			{
				sqlSB.append(" AND ");
				sqlBuilder.appendName(versionField.getName(), sqlSB).append("=?");
			}
			if (log.isDebugEnabled())
			{
				log.debug("prepare: " + sqlSB.toString());
			}

			PreparedStatement pstm = connection.prepareStatement(sqlSB.toString());
			pstm.setQueryTimeout(30);
			return pstm;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, sqlSB.toString());
		}
		finally
		{
			tlObjectCollector.dispose(sqlSB);
		}
	}

	protected PreparedStatement createDeleteStatementWithIn()
	{
		if (deleteStmt != null)
		{
			return deleteStmt;
		}
		IField idField = getIdField();

		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sqlSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			sqlSB.append("DELETE FROM ");
			sqlBuilder.appendName(getName(), sqlSB).append(" WHERE ");
			sqlBuilder.appendName(idField.getName(), sqlSB);
			sqlSB.append("=?");
			deleteStmt = connection.prepareStatement(sqlSB.toString());
			return deleteStmt;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, sqlSB.toString());
		}
		finally
		{
			tlObjectCollector.dispose(sqlSB);
		}
	}

	protected String generateNamesKey(ILinkedMap<String, Object> puis, String[] fieldNames, Object[] values)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		IConversionHelper conversionHelper = this.conversionHelper;
		StringBuilder namesKeySB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			List<IField> allFields = getAllFields();
			for (Entry<String, Object> entry : puis)
			{
				String fieldName = entry.getKey();
				Object newValue = entry.getValue();
				int fieldIndex = getFieldIndexByName(fieldName);
				if (fieldIndex < 0)
				{
					throw new RuntimeException("No field found for member name '" + fieldName + "' on entity '" + getEntityType().getName() + "'");
				}
				IField field = allFields.get(fieldIndex);
				if (newValue == null && java.sql.Array.class.isAssignableFrom(field.getFieldType()))
				{
					newValue = Array.newInstance(field.getFieldSubType(), 0);
				}
				Object convertedValue = conversionHelper.convertValueToType(field.getFieldType(), newValue, field.getFieldSubType());
				values[fieldIndex] = convertedValue;
				fieldNames[fieldIndex] = field.getName();
			}
			for (int a = 0, size = fieldNames.length; a < size; a++)
			{
				String fieldName = fieldNames[a];
				if (fieldName == null)
				{
					continue;
				}
				if (namesKeySB.length() > 0)
				{
					namesKeySB.append('#');
				}
				namesKeySB.append(fieldName);
			}
			return namesKeySB.toString();
		}
		finally
		{
			tlObjectCollector.dispose(namesKeySB);
		}
	}
}
