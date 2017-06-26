package com.koch.ambeth.persistence.jdbc;

import java.io.Closeable;

/*-
 * #%L
 * jambeth-persistence-jdbc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.compositeid.CompositeIdMember;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.OptimisticLockUtil;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.persistence.api.IDirectedLinkMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.sql.CompositeResultSet;
import com.koch.ambeth.persistence.sql.IResultSet;
import com.koch.ambeth.persistence.sql.IResultSetProvider;
import com.koch.ambeth.persistence.sql.SqlTable;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class JdbcTable extends SqlTable {
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IEntityMetaDataProvider entitydMetaDataProvider;

	protected boolean batching = false;

	protected final LinkedHashMap<Object, Object> persistedIdToVersionMap =
			new LinkedHashMap<>();

	protected final LinkedHashMap<Integer, ILinkedMap<String, PreparedStatement>> fieldsToInsertStmtMap =
			new LinkedHashMap<>();

	protected final LinkedHashMap<Integer, ILinkedMap<String, PreparedStatement>> fieldsToUpdateStmtMap =
			new LinkedHashMap<>();

	protected final IdentityHashSet<Object> disposableValues = new IdentityHashSet<>();

	protected PreparedStatement deleteStmt;

	@Property(name = MergeConfigurationConstants.ExactVersionForOptimisticLockingRequired,
			defaultValue = "false")
	protected boolean exactVersionForOptimisticLockingRequired;

	protected int maxInClauseBatchThreshold;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		maxInClauseBatchThreshold = connectionDialect.getMaxInClauseBatchThreshold();
	}

	public void init(ITableMetaData metaData,
			IdentityHashMap<IDirectedLinkMetaData, IDirectedLink> alreadyCreatedLinkMap) {
		this.metaData = metaData;
		for (IDirectedLinkMetaData directedLinkMD : metaData.getLinks()) {
			IDirectedLink directedLink = alreadyCreatedLinkMap.get(directedLinkMD);
			links.add(directedLink);

			linkNameToLinkDict.put(directedLinkMD.getName(), directedLink);
			fieldNameToLinkDict.put(directedLinkMD.getFromField().getName(), directedLink);

			RelationMember member = directedLinkMD.getMember();
			if (member == null) {
				continue;
			}
			memberNameToLinkDict.put(member.getName(), directedLink);
		}
	}

	@Override
	public void startBatch() {
		if (batching) {
			throw new IllegalStateException("Batch queuing already started");
		}
		batching = true;
	}

	// TODO Finding an useful return value
	@Override
	public int[] finishBatch() {
		if (!batching) {
			throw new IllegalStateException("No batch queue open");
		}
		try {
			checkRowLocks(persistedIdToVersionMap);

			if (deleteStmt != null) {
				deleteStmt.executeBatch();
				deleteStmt.close();
				deleteStmt = null;
			}
			executeBatchedStatements(fieldsToUpdateStmtMap);
			executeBatchedStatements(fieldsToInsertStmtMap);
			return new int[0];
		}
		catch (SQLException e) {
			throw connectionDialect.createPersistenceException(e, null);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			clearBatch();
		}
	}

	@Override
	public void clearBatch() {
		for (Object disposableValue : disposableValues) {
			disposeValue(disposableValue);
		}
		disposableValues.clear();
		try {
			fieldsToInsertStmtMap.clear();
			fieldsToUpdateStmtMap.clear();
			persistedIdToVersionMap.clear();
			if (deleteStmt != null) {
				deleteStmt.close();
				deleteStmt = null;
			}
			batching = false;
		}
		catch (SQLException e) {
			// Intended blank
		}
	}

	protected void executeBatchedStatements(
			ILinkedMap<Integer, ILinkedMap<String, PreparedStatement>> fieldsToStmtMap)
			throws SQLException {
		if (fieldsToStmtMap.isEmpty()) {
			return;
		}
		try {
			Iterator<Entry<Integer, ILinkedMap<String, PreparedStatement>>> iter =
					fieldsToStmtMap.iterator();
			while (iter.hasNext()) {
				Entry<Integer, ILinkedMap<String, PreparedStatement>> entry = iter.next();
				for (Entry<String, PreparedStatement> perNamesEntry : entry.getValue()) {
					PreparedStatement prep = perNamesEntry.getValue();
					prep.executeBatch();
				}
			}
		}
		finally {
			Iterator<Entry<Integer, ILinkedMap<String, PreparedStatement>>> iter =
					fieldsToStmtMap.iterator();
			while (iter.hasNext()) {
				Entry<Integer, ILinkedMap<String, PreparedStatement>> entry = iter.next();
				for (Entry<String, PreparedStatement> perNamesEntry : entry.getValue()) {
					PreparedStatement prep = perNamesEntry.getValue();
					try {
						prep.close();
					}
					catch (SQLException e) {
						// Intended blank
					}
				}
			}
			fieldsToStmtMap.clear();
		}
	}

	@Override
	public void delete(List<IObjRef> oris) {
		if (oris == null || oris.isEmpty()) {
			return;
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		ITableMetaData metaData = getMetaData();
		Class<?> entityType = metaData.getEntityType();
		Class<?> idFieldType = metaData.getIdField().getFieldType();
		Class<?> versionFieldType =
				metaData.getVersionField() != null ? metaData.getVersionField().getFieldType() : null;
		PreparedStatement prep = createDeleteStatementWithIn();
		try {
			for (int a = 0, size = oris.size(); a < size; a++) {
				IObjRef ori = oris.get(a);
				if (!entityType.equals(ori.getRealType())) {
					throw new IllegalArgumentException("ORI invalid");
				}
				Object persistenceId = conversionHelper.convertValueToType(idFieldType, ori.getId());
				Object persistenceVersion = null;
				if (versionFieldType != null) {
					Object version = ori.getVersion();
					ParamChecker.assertParamNotNull(version, "version");
					persistenceVersion = conversionHelper.convertValueToType(versionFieldType, version);
				}
				persistedIdToVersionMap.put(persistenceId, persistenceVersion);
				prep.setObject(1, persistenceId);
				prep.addBatch();
			}
		}
		catch (SQLException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			try {
				prep.clearParameters();
			}
			catch (Throwable e) {
				// Intended blank
			}
		}
	}

	protected ILinkedMap<String, PreparedStatement> getEnsureMap(int id,
			LinkedHashMap<Integer, ILinkedMap<String, PreparedStatement>> map) {
		Integer value = Integer.valueOf(id);
		ILinkedMap<String, PreparedStatement> perCount = map.get(value);
		if (perCount == null) {
			perCount = new LinkedHashMap<>();
			map.put(value, perCount);
		}
		return perCount;
	}

	@Override
	public Object insert(Object id, ILinkedMap<IFieldMetaData, Object> puis) {
		ParamChecker.assertParamNotNull(id, "id");
		ParamChecker.assertTrue(batching, "batching");

		ITableMetaData metaData = getMetaData();
		IConversionHelper conversionHelper = this.conversionHelper;

		String[] fieldNames = new String[metaData.getAllFields().size()];
		Object[] values = new Object[fieldNames.length];
		String namesKey = generateNamesKey(puis, fieldNames, values);

		ILinkedMap<String, PreparedStatement> perCount =
				getEnsureMap(puis.size(), fieldsToInsertStmtMap);
		PreparedStatement prep = perCount.get(namesKey);
		if (prep == null) {
			prep = createInsertStatement(fieldNames);
			perCount.put(namesKey, prep);
		}
		IFieldMetaData[] idFields = metaData.getIdFields();
		IFieldMetaData versionField =
				metaData.getVersionField() != null ? metaData.getVersionField() : null;
		Object initialVersion = getMetaData().getInitialVersion();
		Object newVersion = versionField != null ? conversionHelper
				.convertValueToType(versionField.getMember().getRealType(), initialVersion) : null;

		try {
			int index = 1;
			if (idFields.length == 1) {
				prep.setObject(index++,
						conversionHelper.convertValueToType(idFields[0].getFieldType(), id));
			}
			else {
				IEntityMetaData metaData2 = entitydMetaDataProvider.getMetaData(metaData.getEntityType());
				CompositeIdMember idMember = (CompositeIdMember) metaData2.getIdMember();
				for (int a = 0, size = idFields.length; a < size; a++) {
					IFieldMetaData idField = idFields[a];
					prep.setObject(index++, conversionHelper.convertValueToType(idField.getFieldType(),
							idMember.getDecompositedValue(id, a)));
				}
			}
			if (versionField != null) {
				prep.setObject(index++,
						conversionHelper.convertValueToType(versionField.getFieldType(), initialVersion));
			}
			for (int a = 0, size = fieldNames.length; a < size; a++) {
				String fieldName = fieldNames[a];
				if (fieldName == null) {
					// Value not specified
					continue;
				}
				Object convertedValue = values[a];
				prep.setObject(index++, convertedValue);
			}
			IFieldMetaData createdOnField = metaData.getCreatedOnField();
			if (createdOnField != null) {
				Object convertedValue = conversionHelper.convertValueToType(createdOnField.getFieldType(),
						contextProvider.getCurrentTime());
				prep.setObject(index++, convertedValue);
			}
			IFieldMetaData createdByField = metaData.getCreatedByField();
			if (createdByField != null) {
				Object convertedValue = conversionHelper.convertValueToType(createdByField.getFieldType(),
						contextProvider.getCurrentUser());
				prep.setObject(index++, convertedValue);
			}

			prep.addBatch();
		}
		catch (SQLException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			try {
				prep.clearParameters();
			}
			catch (Throwable e) {
				// Intended blank
			}
			for (Object value : values) {
				if (value == null) {
					continue;
				}
				if (value instanceof Blob || value instanceof Clob || value instanceof AutoCloseable
						|| value instanceof Closeable) {
					disposableValues.add(value);
				}
			}
		}

		return newVersion;
	}

	@Override
	public Object update(Object id, Object version, ILinkedMap<IFieldMetaData, Object> puis) {
		ParamChecker.assertParamNotNull(id, "id");
		ITableMetaData metaData = getMetaData();
		IFieldMetaData versionField = metaData.getVersionField();
		if (versionField != null) {
			ParamChecker.assertParamNotNull(version, "version");
		}
		ParamChecker.assertTrue(batching, "batching");
		IConversionHelper conversionHelper = this.conversionHelper;

		String[] fieldNames = new String[metaData.getAllFields().size()];
		Object[] values = new Object[fieldNames.length];
		String namesKey = generateNamesKey(puis, fieldNames, values);

		ILinkedMap<String, PreparedStatement> perCount =
				getEnsureMap(puis.size(), fieldsToUpdateStmtMap);
		PreparedStatement prep = perCount.get(namesKey);
		if (prep == null) {
			prep = createUpdateStatement(fieldNames);
			perCount.put(namesKey, prep);
		}

		Object newVersion = null;
		if (versionField != null) {
			Class<?> versionFieldType = versionField.getFieldType();
			version = conversionHelper.convertValueToType(versionFieldType, version);

			if (versionFieldType.equals(Long.class)) {
				newVersion = Long.valueOf((Long) version + 1);
			}
			else if (versionFieldType.equals(Integer.class)) {
				newVersion = Integer.valueOf((Integer) version + 1);
			}
			else if (versionFieldType.equals(Short.class)) {
				newVersion = Short.valueOf((short) ((Short) version + 1));
			}
			else if (versionFieldType.equals(Byte.class)) {
				newVersion = Byte.valueOf((byte) ((Byte) version + 1));
			}
			else if (versionFieldType.equals(Date.class)) {
				newVersion = new Date(contextProvider.getCurrentTime());
			}
			else if (versionFieldType.equals(BigInteger.class)) {
				newVersion = ((BigInteger) version).add(BigInteger.ONE);
			}
			else if (versionFieldType.equals(BigDecimal.class)) {
				newVersion = ((BigDecimal) version).add(BigDecimal.ONE);
			}
			else {
				throw new IllegalArgumentException("Version type not supported: " + version.getClass());
			}
		}

		try {
			int index = 1;
			if (versionField != null) {
				prep.setObject(index++,
						conversionHelper.convertValueToType(versionField.getFieldType(), newVersion));
			}
			for (int a = 0, size = fieldNames.length; a < size; a++) {
				String fieldName = fieldNames[a];
				if (fieldName == null) {
					// Value not specified
					continue;
				}
				Object convertedValue = values[a];
				prep.setObject(index++, convertedValue);
			}
			IFieldMetaData updatedOnField = metaData.getUpdatedOnField();
			if (updatedOnField != null) {
				Object convertedValue = conversionHelper.convertValueToType(updatedOnField.getFieldType(),
						contextProvider.getCurrentTime());
				prep.setObject(index++, convertedValue);
			}
			IFieldMetaData updatedByField = metaData.getUpdatedByField();
			if (updatedByField != null) {
				Object convertedValue = conversionHelper.convertValueToType(updatedByField.getFieldType(),
						contextProvider.getCurrentUser());
				prep.setObject(index++, convertedValue);
			}

			Object persistenceId =
					conversionHelper.convertValueToType(metaData.getIdField().getFieldType(), id);
			Object persistenceVersion = null;
			prep.setObject(index++, persistenceId);
			if (versionField != null) {
				persistenceVersion =
						conversionHelper.convertValueToType(versionField.getFieldType(), version);
			}
			if (connectionDialect.useVersionOnOptimisticUpdate()) {
				prep.setObject(index++, persistenceVersion);
			}
			prep.addBatch();

			persistedIdToVersionMap.put(persistenceId, persistenceVersion);
		}
		catch (SQLException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			try {
				prep.clearParameters();
			}
			catch (Throwable e) {
				// Intended blank
			}
			for (Object value : values) {
				if (value == null) {
					continue;
				}
				if (value instanceof Blob || value instanceof Clob || value instanceof AutoCloseable
						|| value instanceof Closeable) {
					disposableValues.add(value);
				}
			}
		}
		return newVersion;
	}

	protected void disposeValue(Object value) {
		if (value == null) {
			return;
		}
		try {
			if (value instanceof Blob) {
				connectionDialect.releaseBlob((Blob) value);
			}
			else if (value instanceof Clob) {
				connectionDialect.releaseClob((Clob) value);
			}
			else if (value instanceof java.sql.Array) {
				connectionDialect.releaseArray((java.sql.Array) value);
			}
			else if (value instanceof AutoCloseable) {
				((AutoCloseable) value).close();
			}
			else if (value instanceof Closeable) {
				((Closeable) value).close();
			}
		}
		catch (Throwable e) {
			// intended blank
		}
	}

	@SuppressWarnings("unchecked")
	protected void checkRowLocks(ILinkedMap<Object, Object> persistedIdToVersionMap)
			throws SQLException {
		if (persistedIdToVersionMap.isEmpty()) {
			return;
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		ITableMetaData metaData = getMetaData();
		boolean exactVersionForOptimisticLockingRequired =
				this.exactVersionForOptimisticLockingRequired;
		Class<?> idFieldType = metaData.getIdField().getFieldType();

		List<Object> persistedIdsForArray = persistedIdToVersionMap.keyList();

		Class<?> versionFieldType =
				metaData.getVersionField() != null ? metaData.getVersionField().getFieldType() : null;
		IResultSet selectForUpdateRS = createSelectForUpdateStatementWithIn(persistedIdsForArray);
		try {
			while (selectForUpdateRS.moveNext()) {
				Object[] current = selectForUpdateRS.getCurrent();
				Object persistedId = conversionHelper.convertValueToType(idFieldType, current[0]);
				Object givenPersistedVersion = persistedIdToVersionMap.remove(persistedId);
				if (versionFieldType == null) {
					continue;
				}
				Object persistedVersion = conversionHelper.convertValueToType(versionFieldType, current[1]);
				if (log.isDebugEnabled()) {
					log.debug("Given: " + metaData.getName() + " - " + persistedId + ", Version: "
							+ givenPersistedVersion + ", VersionInDb: " + persistedVersion);
				}

				if (persistedVersion == null) {
					continue;
				}
				if (exactVersionForOptimisticLockingRequired) {
					if (!persistedVersion.equals(givenPersistedVersion)) {
						Object objId = conversionHelper
								.convertValueToType(metaData.getIdField().getMember().getRealType(), persistedId);
						Object objVersion = conversionHelper.convertValueToType(
								metaData.getVersionField().getMember().getRealType(), persistedVersion);
						throw OptimisticLockUtil.throwModified(
								new ObjRef(metaData.getEntityType(), objId, objVersion), givenPersistedVersion);
					}
				}
				else {
					if (((Comparable<Object>) persistedVersion).compareTo(givenPersistedVersion) > 0) {
						Object objId = conversionHelper
								.convertValueToType(metaData.getIdField().getMember().getRealType(), persistedId);
						Object objVersion = conversionHelper.convertValueToType(
								metaData.getVersionField().getMember().getRealType(), persistedVersion);
						throw OptimisticLockUtil.throwModified(
								new ObjRef(metaData.getEntityType(), objId, objVersion), givenPersistedVersion);
					}
				}
			}
			if (!persistedIdToVersionMap.isEmpty()) {
				Object objId =
						conversionHelper.convertValueToType(metaData.getIdField().getMember().getRealType(),
								persistedIdToVersionMap.iterator().next().getKey());
				throw OptimisticLockUtil.throwDeleted(new ObjRef(metaData.getEntityType(), objId, null));
			}
		}
		finally {
			selectForUpdateRS.dispose();
		}
	}

	protected PreparedStatement createInsertStatement(String[] fieldNames) {
		ITableMetaData metaData = getMetaData();
		IFieldMetaData[] idFields = metaData.getIdFields();
		IFieldMetaData versionField = metaData.getVersionField();

		int variableCount = 0;
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		AppendableStringBuilder sqlSB = tlObjectCollector.create(AppendableStringBuilder.class);
		try {
			sqlSB.append("INSERT INTO ");
			sqlBuilder.appendName(metaData.getFullqualifiedEscapedName(), sqlSB).append(" (");
			for (IFieldMetaData idField : idFields) {
				if (variableCount > 0) {
					sqlSB.append(',');
				}
				sqlBuilder.appendName(idField.getName(), sqlSB);
				variableCount++;
			}
			if (versionField != null) {
				sqlSB.append(',');
				sqlBuilder.appendName(versionField.getName(), sqlSB);
				variableCount++;
			}

			for (int a = 0, size = fieldNames.length; a < size; a++) {
				String fieldName = fieldNames[a];
				if (fieldName == null) {
					// Value not specified
					continue;
				}
				sqlSB.append(',');
				sqlBuilder.appendName(fieldName, sqlSB);
				variableCount++;
			}
			IFieldMetaData createdOnField = metaData.getCreatedOnField();
			if (createdOnField != null) {
				sqlSB.append(',');
				sqlBuilder.appendName(createdOnField.getName(), sqlSB);
				variableCount++;
			}
			IFieldMetaData createdByField = metaData.getCreatedByField();
			if (createdByField != null) {
				sqlSB.append(',');
				sqlBuilder.appendName(createdByField.getName(), sqlSB);
				variableCount++;
			}
			sqlSB.append(") VALUES (");
			boolean first = true;
			for (int a = variableCount; a-- > 0;) {
				if (first) {
					first = false;
					sqlSB.append("?");
				}
				else {
					sqlSB.append(",?");
				}
			}
			sqlSB.append(')');

			String sql = sqlSB.toString();
			if (log.isDebugEnabled()) {
				log.debug("prepare: " + sql);
			}

			return connection.prepareStatement(sql);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e, sqlSB.toString());
		}
		finally {
			tlObjectCollector.dispose(sqlSB);
		}
	}

	protected IResultSet createSelectForUpdateStatementWithIn(List<Object> ids) {
		if (ids.size() <= maxInClauseBatchThreshold) {
			return createSelectForUpdateStatementWithInIntern(ids);
		}
		IList<IList<Object>> splitValues =
				persistenceHelper.splitValues(ids, maxInClauseBatchThreshold);

		ArrayList<IResultSetProvider> resultSetProviderStack = new ArrayList<>(splitValues.size());
		// Stack gets evaluated last->first so back iteration is correct to execute the sql in order
		// later
		for (int a = splitValues.size(); a-- > 0;) {
			final IList<Object> values = splitValues.get(a);
			resultSetProviderStack.add(new IResultSetProvider() {
				@Override
				public void skipResultSet() {
					// Intended blank
				}

				@Override
				public IResultSet getResultSet() {
					return createSelectForUpdateStatementWithInIntern(values);
				}
			});
		}
		CompositeResultSet compositeResultSet = new CompositeResultSet();
		compositeResultSet.setResultSetProviderStack(resultSetProviderStack);
		compositeResultSet.afterPropertiesSet();
		return compositeResultSet;
	}

	protected IResultSet createSelectForUpdateStatementWithInIntern(List<?> ids) {
		ITableMetaData metaData = getMetaData();
		IFieldMetaData idField = metaData.getIdField();
		IFieldMetaData versionField = metaData.getVersionField();
		ArrayList<Object> parameters = new ArrayList<>();
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		AppendableStringBuilder fieldNamesSQL = tlObjectCollector.create(AppendableStringBuilder.class);
		AppendableStringBuilder whereSQL = tlObjectCollector.create(AppendableStringBuilder.class);
		try {
			sqlBuilder.appendName(idField.getName(), fieldNamesSQL);
			if (versionField != null) {
				fieldNamesSQL.append(',');
				sqlBuilder.appendName(versionField.getName(), fieldNamesSQL);
			}
			persistenceHelper.appendSplittedValues(idField.getName(), idField.getFieldType(), ids,
					parameters, whereSQL);
			whereSQL.append(connectionDialect.getSelectForUpdateFragment());

			return sqlConnection.selectFields(metaData.getFullqualifiedEscapedName(), fieldNamesSQL,
					whereSQL, null, null, parameters);
		}
		finally {
			tlObjectCollector.dispose(whereSQL);
			tlObjectCollector.dispose(fieldNamesSQL);
		}
	}

	protected PreparedStatement createUpdateStatement(String[] fieldNames) {
		ITableMetaData metaData = getMetaData();
		IFieldMetaData idField = metaData.getIdField();
		IFieldMetaData versionField = metaData.getVersionField();

		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		AppendableStringBuilder sqlSB = tlObjectCollector.create(AppendableStringBuilder.class);
		try {
			sqlSB.append("UPDATE ");
			sqlBuilder.appendName(metaData.getFullqualifiedEscapedName(), sqlSB);
			sqlSB.append(" SET ");

			boolean firstField = true;
			if (versionField != null) {
				firstField = false;
				sqlBuilder.appendName(versionField.getName(), sqlSB).append("=?");
			}

			for (int a = 0, size = fieldNames.length; a < size; a++) {
				String fieldName = fieldNames[a];
				if (fieldName == null) {
					// Value not specified
					continue;
				}
				if (!firstField) {
					sqlSB.append(',');
				}
				firstField = false;
				sqlBuilder.appendName(fieldName, sqlSB).append("=?");
			}
			IFieldMetaData updatedOnField = metaData.getUpdatedOnField();
			if (updatedOnField != null) {
				if (!firstField) {
					sqlSB.append(',');
				}
				firstField = false;
				sqlBuilder.appendName(updatedOnField.getName(), sqlSB).append("=?");
			}
			IFieldMetaData updatedByField = metaData.getUpdatedByField();
			if (updatedByField != null) {
				if (!firstField) {
					sqlSB.append(',');
				}
				firstField = false;
				sqlBuilder.appendName(updatedByField.getName(), sqlSB).append("=?");
			}
			sqlSB.append(" WHERE ");
			sqlBuilder.appendName(idField.getName(), sqlSB).append("=?");
			if (connectionDialect.useVersionOnOptimisticUpdate() && versionField != null) {
				sqlSB.append(" AND ");
				sqlBuilder.appendName(versionField.getName(), sqlSB).append("=?");
			}
			if (log.isDebugEnabled()) {
				log.debug("prepare: " + sqlSB.toString());
			}

			return connection.prepareStatement(sqlSB.toString());
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e, sqlSB.toString());
		}
		finally {
			tlObjectCollector.dispose(sqlSB);
		}
	}

	protected PreparedStatement createDeleteStatementWithIn() {
		if (deleteStmt != null) {
			return deleteStmt;
		}
		ITableMetaData metaData = getMetaData();
		IFieldMetaData idField = metaData.getIdField();

		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		AppendableStringBuilder sqlSB = tlObjectCollector.create(AppendableStringBuilder.class);
		try {
			sqlSB.append("DELETE FROM ");
			sqlBuilder.appendName(metaData.getFullqualifiedEscapedName(), sqlSB).append(" WHERE ");
			sqlBuilder.appendName(idField.getName(), sqlSB);
			sqlSB.append("=?");
			deleteStmt = connection.prepareStatement(sqlSB.toString());
			return deleteStmt;
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e, sqlSB.toString());
		}
		finally {
			tlObjectCollector.dispose(sqlSB);
		}
	}

	protected String generateNamesKey(ILinkedMap<IFieldMetaData, Object> puis, String[] fieldNames,
			Object[] values) {
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder namesKeySB = tlObjectCollector.create(StringBuilder.class);
		try {
			for (Entry<IFieldMetaData, Object> entry : puis) {
				IFieldMetaData field = entry.getKey();
				Object newValue = entry.getValue();
				if (newValue == null && java.sql.Array.class.isAssignableFrom(field.getFieldType())) {
					newValue = Array.newInstance(field.getFieldSubType(), 0);
				}
				Object convertedValue = connectionDialect.convertToFieldType(field, newValue);
				int fieldIndex = field.getIndexOnTable();
				values[fieldIndex] = convertedValue;
				fieldNames[fieldIndex] = field.getName();
			}
			for (int a = 0, size = fieldNames.length; a < size; a++) {
				String fieldName = fieldNames[a];
				if (fieldName == null) {
					continue;
				}
				if (namesKeySB.length() > 0) {
					namesKeySB.append('#');
				}
				namesKeySB.append(fieldName);
			}
			return namesKeySB.toString();
		}
		finally {
			tlObjectCollector.dispose(namesKeySB);
		}
	}
}
