package com.koch.ambeth.persistence.jdbc;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.compositeid.CompositeIdMember;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
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
import com.koch.ambeth.persistence.sql.IdContainerImpl;
import com.koch.ambeth.persistence.sql.SqlTable;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IParamHolder;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedBiFunction;
import lombok.SneakyThrows;

import java.io.Closeable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class JdbcTable extends SqlTable {
    protected final LinkedHashMap<Object, Object> persistedIdToVersionMap = new LinkedHashMap<>();
    protected final LinkedHashMap<Integer, ILinkedMap<String, PreparedStatement>> fieldsToInsertStmtMap = new LinkedHashMap<>();
    protected final LinkedHashMap<Integer, ILinkedMap<String, PreparedStatement>> fieldsToUpdateStmtMap = new LinkedHashMap<>();
    protected final IdentityHashSet<Object> disposableValues = new IdentityHashSet<>();
    @Autowired
    protected ICompositeIdFactory compositeIdFactory;
    @Autowired
    protected Connection connection;
    @Autowired
    protected IConnectionDialect connectionDialect;
    protected boolean batching = false;
    protected PreparedStatement deleteStmt;
    @Property(name = MergeConfigurationConstants.ExactVersionForOptimisticLockingRequired, defaultValue = "false")
    protected boolean exactVersionForOptimisticLockingRequired;
    protected int maxInClauseBatchThreshold;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        super.afterPropertiesSet();

        maxInClauseBatchThreshold = connectionDialect.getMaxInClauseBatchThreshold();
    }

    public void init(ITableMetaData metaData, IdentityHashMap<IDirectedLinkMetaData, IDirectedLink> alreadyCreatedLinkMap) {
        this.metaData = metaData;
        for (var directedLinkMD : metaData.getLinks()) {
            var directedLink = alreadyCreatedLinkMap.get(directedLinkMD);
            links.add(directedLink);

            linkNameToLinkDict.put(directedLinkMD.getName(), directedLink);
            fieldNameToLinkDict.put(directedLinkMD.getFromField().getName(), directedLink);

            var member = directedLinkMD.getMember();
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
        } catch (SQLException e) {
            throw connectionDialect.createPersistenceException(e, null);
        } finally {
            clearBatch();
        }
    }

    @Override
    public void clearBatch() {
        for (var disposableValue : disposableValues) {
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
        } catch (SQLException e) {
            // Intended blank
        }
    }

    protected void executeBatchedStatements(ILinkedMap<Integer, ILinkedMap<String, PreparedStatement>> fieldsToStmtMap) throws SQLException {
        if (fieldsToStmtMap.isEmpty()) {
            return;
        }
        try {
            var iter = fieldsToStmtMap.iterator();
            while (iter.hasNext()) {
                var entry = iter.next();
                for (var perNamesEntry : entry.getValue()) {
                    var prep = perNamesEntry.getValue();
                    prep.executeBatch();
                }
            }
        } finally {
            var iter = fieldsToStmtMap.iterator();
            while (iter.hasNext()) {
                var entry = iter.next();
                for (var perNamesEntry : entry.getValue()) {
                    var prep = perNamesEntry.getValue();
                    try {
                        prep.close();
                    } catch (SQLException e) {
                        // Intended blank
                    }
                }
            }
            fieldsToStmtMap.clear();
        }
    }

    @SneakyThrows
    @Override
    public void delete(List<IObjRef> oris) {
        if (oris == null || oris.isEmpty()) {
            return;
        }
        var conversionHelper = this.conversionHelper;
        var tableMetaData = getMetaData();
        var entityType = tableMetaData.getEntityType();
        var idFields = tableMetaData.getIdFields();
        CheckedBiFunction<PreparedStatement, Object, Object> idHandler;
        if (idFields.length == 1) {
            var elementType = idFields[0].getMember().getElementType();
            var fieldType = idFields[0].getFieldType();
            idHandler = (pstm, id) -> {
                var sqlId = conversionHelper.convertValueToType(fieldType, id);
                pstm.setObject(1, sqlId);
                return conversionHelper.convertValueToType(elementType, id);
            };
        } else {
            var metaData = entityMetaDataProvider.getMetaData(tableMetaData.getEntityType());
            var idMember = (CompositeIdMember) metaData.getIdMember();
            idHandler = (pstm, id) -> {
                for (int compositeIdIndex = idFields.length; compositeIdIndex-- > 0; ) {
                    var sqlId = conversionHelper.convertValueToType(idFields[compositeIdIndex].getFieldType(), idMember.getDecompositedValue(id, compositeIdIndex));
                    pstm.setObject(compositeIdIndex + 1, sqlId);
                }
                return id;
            };
        }
        var versionFieldType = tableMetaData.getVersionField() != null ? tableMetaData.getVersionField().getFieldType() : null;
        var prep = createDeleteStatementWithIn();
        try {
            for (int a = 0, size = oris.size(); a < size; a++) {
                var ori = oris.get(a);
                if (!entityType.equals(ori.getRealType())) {
                    throw new IllegalArgumentException("ORI invalid");
                }
                Object persistenceVersion = null;
                if (versionFieldType != null) {
                    var version = ori.getVersion();
                    ParamChecker.assertParamNotNull(version, "version");
                    persistenceVersion = conversionHelper.convertValueToType(versionFieldType, version);
                }
                var persistenceId = idHandler.apply(prep, ori.getId());
                persistedIdToVersionMap.put(persistenceId, persistenceVersion);
                prep.addBatch();
            }
        } finally {
            try {
                prep.clearParameters();
            } catch (Throwable e) {
                // Intended blank
            }
        }
    }

    protected ILinkedMap<String, PreparedStatement> getEnsureMap(int id, LinkedHashMap<Integer, ILinkedMap<String, PreparedStatement>> map) {
        var value = Integer.valueOf(id);
        var perCount = map.get(value);
        if (perCount == null) {
            perCount = new LinkedHashMap<>();
            map.put(value, perCount);
        }
        return perCount;
    }

    @SneakyThrows
    @Override
    public Object insert(Object id, ILinkedMap<IFieldMetaData, Object> puis) {
        ParamChecker.assertParamNotNull(id, "id");
        ParamChecker.assertTrue(batching, "batching");

        var metaData = getMetaData();
        var conversionHelper = this.conversionHelper;

        var fieldNames = new String[metaData.getAllFields().size()];
        var values = new Object[fieldNames.length];
        var namesKey = generateNamesKey(puis, fieldNames, values);

        var perCount = getEnsureMap(puis.size(), fieldsToInsertStmtMap);
        var prep = perCount.get(namesKey);
        if (prep == null) {
            prep = createInsertStatement(fieldNames);
            perCount.put(namesKey, prep);
        }
        var idFields = metaData.getIdFields();
        var versionField = metaData.getVersionField() != null ? metaData.getVersionField() : null;
        var initialVersion = getMetaData().getInitialVersion();
        var newVersion = versionField != null ? conversionHelper.convertValueToType(versionField.getMember().getRealType(), initialVersion) : null;

        try {
            var index = 1;
            if (idFields.length == 1) {
                prep.setObject(index++, conversionHelper.convertValueToType(idFields[0].getFieldType(), id));
            } else {
                var metaData2 = entityMetaDataProvider.getMetaData(metaData.getEntityType());
                var idMember = (CompositeIdMember) metaData2.getIdMember();
                for (int compositeIdIndex = 0, size = idFields.length; compositeIdIndex < size; compositeIdIndex++) {
                    var idField = idFields[compositeIdIndex];
                    prep.setObject(index++, conversionHelper.convertValueToType(idField.getFieldType(), idMember.getDecompositedValue(id, compositeIdIndex)));
                }
            }
            if (versionField != null) {
                prep.setObject(index++, conversionHelper.convertValueToType(versionField.getFieldType(), initialVersion));
            }
            for (int a = 0, size = fieldNames.length; a < size; a++) {
                var fieldName = fieldNames[a];
                if (fieldName == null) {
                    // Value not specified
                    continue;
                }
                var convertedValue = values[a];
                prep.setObject(index++, convertedValue);
            }
            var createdOnField = metaData.getCreatedOnField();
            if (createdOnField != null) {
                var convertedValue = conversionHelper.convertValueToType(createdOnField.getFieldType(), contextProvider.getCurrentTime());
                prep.setObject(index++, convertedValue);
            }
            var createdByField = metaData.getCreatedByField();
            if (createdByField != null) {
                var convertedValue = conversionHelper.convertValueToType(createdByField.getFieldType(), contextProvider.getCurrentUser());
                prep.setObject(index++, convertedValue);
            }

            prep.addBatch();
        } finally {
            try {
                prep.clearParameters();
            } catch (Throwable e) {
                // Intended blank
            }
            for (Object value : values) {
                if (value == null) {
                    continue;
                }
                if (value instanceof Blob || value instanceof Clob || value instanceof AutoCloseable || value instanceof Closeable) {
                    disposableValues.add(value);
                }
            }
        }

        return newVersion;
    }

    @SneakyThrows
    @Override
    public Object update(Object id, Object version, ILinkedMap<IFieldMetaData, Object> puis) {
        ParamChecker.assertParamNotNull(id, "id");
        var tableMetaData = getMetaData();
        var versionField = tableMetaData.getVersionField();
        if (versionField != null) {
            ParamChecker.assertParamNotNull(version, "version");
        }
        ParamChecker.assertTrue(batching, "batching");
        var conversionHelper = this.conversionHelper;

        var fieldNames = new String[tableMetaData.getAllFields().size()];
        var values = new Object[fieldNames.length];
        var namesKey = generateNamesKey(puis, fieldNames, values);

        var perCount = getEnsureMap(puis.size(), fieldsToUpdateStmtMap);
        var prep = perCount.get(namesKey);
        if (prep == null) {
            prep = createUpdateStatement(fieldNames);
            perCount.put(namesKey, prep);
        }

        Object newVersion = null;
        if (versionField != null) {
            var versionFieldType = versionField.getFieldType();
            version = conversionHelper.convertValueToType(versionFieldType, version);

            if (versionFieldType.equals(Long.class)) {
                newVersion = Long.valueOf((Long) version + 1);
            } else if (versionFieldType.equals(Integer.class)) {
                newVersion = Integer.valueOf((Integer) version + 1);
            } else if (versionFieldType.equals(Short.class)) {
                newVersion = Short.valueOf((short) ((Short) version + 1));
            } else if (versionFieldType.equals(Byte.class)) {
                newVersion = Byte.valueOf((byte) ((Byte) version + 1));
            } else if (versionFieldType.equals(Date.class)) {
                newVersion = new Date(contextProvider.getCurrentTime());
            } else if (versionFieldType.equals(BigInteger.class)) {
                newVersion = ((BigInteger) version).add(BigInteger.ONE);
            } else if (versionFieldType.equals(BigDecimal.class)) {
                newVersion = ((BigDecimal) version).add(BigDecimal.ONE);
            } else {
                throw new IllegalArgumentException("Version type not supported: " + version.getClass());
            }
        }

        try {
            var index = 1;
            if (versionField != null) {
                prep.setObject(index++, conversionHelper.convertValueToType(versionField.getFieldType(), newVersion));
            }
            for (int a = 0, size = fieldNames.length; a < size; a++) {
                var fieldName = fieldNames[a];
                if (fieldName == null) {
                    // Value not specified
                    continue;
                }
                var convertedValue = values[a];
                prep.setObject(index++, convertedValue);
            }
            var updatedOnField = tableMetaData.getUpdatedOnField();
            if (updatedOnField != null) {
                Object convertedValue = conversionHelper.convertValueToType(updatedOnField.getFieldType(), contextProvider.getCurrentTime());
                prep.setObject(index++, convertedValue);
            }
            var updatedByField = tableMetaData.getUpdatedByField();
            if (updatedByField != null) {
                Object convertedValue = conversionHelper.convertValueToType(updatedByField.getFieldType(), contextProvider.getCurrentUser());
                prep.setObject(index++, convertedValue);
            }
            Object persistenceId;
            var idFields = tableMetaData.getIdFields();
            if (idFields.length == 1) {
                var idField = idFields[0];
                var sqlId = conversionHelper.convertValueToType(idField.getFieldType(), id);
                persistenceId = conversionHelper.convertValueToType(idField.getMember().getElementType(), id);
                prep.setObject(index++, sqlId);
            } else {
                persistenceId = id;
                var metaData = entityMetaDataProvider.getMetaData(tableMetaData.getEntityType());
                var idMember = (CompositeIdMember) metaData.getIdMember();
                for (int compositeIdIndex = 0, size = idFields.length; compositeIdIndex < size; compositeIdIndex++) {
                    var idField = idFields[compositeIdIndex];
                    var sqlId = conversionHelper.convertValueToType(idField.getFieldType(), idMember.getDecompositedValue(id, compositeIdIndex));
                    prep.setObject(index++, sqlId);
                }
            }
            Object persistenceVersion = null;
            if (versionField != null) {
                persistenceVersion = conversionHelper.convertValueToType(versionField.getFieldType(), version);
            }
            if (connectionDialect.useVersionOnOptimisticUpdate()) {
                prep.setObject(index++, persistenceVersion);
            }
            prep.addBatch();
            persistedIdToVersionMap.put(persistenceId, persistenceVersion);
        } finally {
            try {
                prep.clearParameters();
            } catch (Throwable e) {
                // Intended blank
            }
            for (var value : values) {
                if (value == null) {
                    continue;
                }
                if (value instanceof Blob || value instanceof Clob || value instanceof AutoCloseable || value instanceof Closeable) {
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
            } else if (value instanceof Clob) {
                connectionDialect.releaseClob((Clob) value);
            } else if (value instanceof java.sql.Array) {
                connectionDialect.releaseArray((java.sql.Array) value);
            } else if (value instanceof AutoCloseable) {
                ((AutoCloseable) value).close();
            } else if (value instanceof Closeable) {
                ((Closeable) value).close();
            }
        } catch (Throwable e) {
            // intended blank
        }
    }

    @SuppressWarnings("unchecked")
    protected void checkRowLocks(ILinkedMap<Object, Object> persistedIdToVersionMap) throws SQLException {
        if (persistedIdToVersionMap.isEmpty()) {
            return;
        }
        var conversionHelper = this.conversionHelper;
        var tableMetaData = getMetaData();
        var exactVersionForOptimisticLockingRequired = this.exactVersionForOptimisticLockingRequired;
        var metaData = entityMetaDataProvider.getMetaData(tableMetaData.getEntityType());
        var idConverter = compositeIdFactory.prepareCompositeIdFactory(metaData, metaData.getIdMember());
        var versionConverter = tableMetaData.getVersionField() != null ? conversionHelper.prepareConverter(tableMetaData.getVersionField().getFieldType()) : null;
        var persistedIdsForArray = persistedIdToVersionMap.keyList();
        var versionIndexPH = new ParamHolder<Integer>();
        var selectForUpdateRS = createSelectForUpdateStatementWithIn(persistedIdsForArray, versionIndexPH);
        try {
            for (var current : selectForUpdateRS) {
                var persistedId = idConverter.convertValue(current, null);
                var sizeBeforeRemoval = persistedIdToVersionMap.size();
                var givenPersistedVersion = persistedIdToVersionMap.remove(persistedId);
                if (versionConverter == null) {
                    // confirm that the entry was really removed even if we dont have a managed version entry
                    var sizeAfterRemoval = persistedIdToVersionMap.size();
                    if (sizeBeforeRemoval == sizeAfterRemoval) {
                        throw new IllegalStateException("Entry with id " + persistedId + " not found in state for table " + tableMetaData.getName());
                    }
                    continue;
                }
                var persistedVersion = versionConverter.convertValue(current[versionIndexPH.getValue()], null);
                if (log.isDebugEnabled()) {
                    log.debug("Given: " + tableMetaData.getName() + " - " + persistedId + ", Version: " + givenPersistedVersion + ", VersionInDb: " + persistedVersion);
                }

                if (persistedVersion == null) {
                    continue;
                }
                if (exactVersionForOptimisticLockingRequired) {
                    if (!persistedVersion.equals(givenPersistedVersion)) {
                        var objId = conversionHelper.convertValueToType(tableMetaData.getIdField().getMember().getRealType(), persistedId);
                        var objVersion = conversionHelper.convertValueToType(tableMetaData.getVersionField().getMember().getRealType(), persistedVersion);
                        throw OptimisticLockUtil.throwModified(new ObjRef(tableMetaData.getEntityType(), objId, objVersion), givenPersistedVersion);
                    }
                } else {
                    if (((Comparable<Object>) persistedVersion).compareTo(givenPersistedVersion) > 0) {
                        var objId = conversionHelper.convertValueToType(tableMetaData.getIdField().getMember().getRealType(), persistedId);
                        var objVersion = conversionHelper.convertValueToType(tableMetaData.getVersionField().getMember().getRealType(), persistedVersion);
                        throw OptimisticLockUtil.throwModified(new ObjRef(tableMetaData.getEntityType(), objId, objVersion), givenPersistedVersion);
                    }
                }
            }
            if (!persistedIdToVersionMap.isEmpty()) {
                var objId = conversionHelper.convertValueToType(tableMetaData.getIdField().getMember().getRealType(), persistedIdToVersionMap.iterator().next().getKey());
                throw OptimisticLockUtil.throwDeleted(new ObjRef(tableMetaData.getEntityType(), objId, null));
            }
        } finally {
            selectForUpdateRS.dispose();
        }
    }

    protected PreparedStatement createInsertStatement(String[] fieldNames) {
        var metaData = getMetaData();
        var idFields = metaData.getIdFields();
        var versionField = metaData.getVersionField();

        var variableCount = 0;
        var tlObjectCollector = objectCollector.getCurrent();
        var sqlSB = tlObjectCollector.create(AppendableStringBuilder.class);
        try {
            sqlSB.append("INSERT INTO ");
            sqlBuilder.appendName(metaData.getFullqualifiedEscapedName(), sqlSB).append(" (");
            for (var idField : idFields) {
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
                var fieldName = fieldNames[a];
                if (fieldName == null) {
                    // Value not specified
                    continue;
                }
                sqlSB.append(',');
                sqlBuilder.appendName(fieldName, sqlSB);
                variableCount++;
            }
            var createdOnField = metaData.getCreatedOnField();
            if (createdOnField != null) {
                sqlSB.append(',');
                sqlBuilder.appendName(createdOnField.getName(), sqlSB);
                variableCount++;
            }
            var createdByField = metaData.getCreatedByField();
            if (createdByField != null) {
                sqlSB.append(',');
                sqlBuilder.appendName(createdByField.getName(), sqlSB);
                variableCount++;
            }
            sqlSB.append(") VALUES (");
            var first = true;
            for (int a = variableCount; a-- > 0; ) {
                if (first) {
                    first = false;
                    sqlSB.append("?");
                } else {
                    sqlSB.append(",?");
                }
            }
            sqlSB.append(')');

            var sql = sqlSB.toString();
            if (log.isDebugEnabled()) {
                log.debug("prepare: " + sql);
            }

            return connection.prepareStatement(sql);
        } catch (Throwable e) {
            throw RuntimeExceptionUtil.mask(e, sqlSB.toString());
        } finally {
            tlObjectCollector.dispose(sqlSB);
        }
    }

    protected IResultSet createSelectForUpdateStatementWithIn(List<Object> ids, IParamHolder<Integer> versionIndexPH) {
        if (ids.size() <= maxInClauseBatchThreshold) {
            return createSelectForUpdateStatementWithInIntern(ids, versionIndexPH);
        }
        var splitValues = persistenceHelper.splitValues(ids, maxInClauseBatchThreshold);

        var resultSetProviderStack = new ArrayList<IResultSetProvider>(splitValues.size());
        // Stack gets evaluated last->first so back iteration is correct to execute the sql in order
        // later
        for (int a = splitValues.size(); a-- > 0; ) {
            var values = splitValues.get(a);
            resultSetProviderStack.add(() -> createSelectForUpdateStatementWithInIntern(values, versionIndexPH));
        }
        var compositeResultSet = new CompositeResultSet();
        compositeResultSet.setResultSetProviderStack(resultSetProviderStack);
        compositeResultSet.afterPropertiesSet();
        return compositeResultSet;
    }

    protected IResultSet createSelectForUpdateStatementWithInIntern(List<?> ids, IParamHolder<Integer> versionIndexPH) {
        var tableMetaData = getMetaData();
        var parameters = new ArrayList<>();
        var tlObjectCollector = objectCollector.getCurrent();
        var fieldNamesSQL = tlObjectCollector.create(AppendableStringBuilder.class);
        var whereSQL = tlObjectCollector.create(AppendableStringBuilder.class);
        try {
            var selectState = new SelectState(fieldNamesSQL);
            appendSelectPrimaryIds(tableMetaData, null, false, null, selectState);
            appendSelectVersion(tableMetaData, null, selectState);

            persistenceHelper.appendSplittedValues(IdContainerImpl.ofIdIndex(tableMetaData.getIdFields(), ids, tableMetaData.getEntityType(), entityMetaDataProvider), parameters, whereSQL);
            whereSQL.append(connectionDialect.getSelectForUpdateFragment());

            versionIndexPH.setValue(selectState.getVersionIndex());
            return sqlConnection.selectFields(tableMetaData.getFullqualifiedEscapedName(), fieldNamesSQL, whereSQL, null, null, parameters);
        } finally {
            tlObjectCollector.dispose(whereSQL);
            tlObjectCollector.dispose(fieldNamesSQL);
        }
    }

    protected PreparedStatement createUpdateStatement(String[] fieldNames) {
        var metaData = getMetaData();
        var idFields = metaData.getIdFields();
        var versionField = metaData.getVersionField();

        var tlObjectCollector = objectCollector.getCurrent();
        var sqlSB = tlObjectCollector.create(AppendableStringBuilder.class);
        try {
            sqlSB.append("UPDATE ");
            sqlBuilder.appendName(metaData.getFullqualifiedEscapedName(), sqlSB);
            sqlSB.append(" SET ");

            var firstField = true;
            if (versionField != null) {
                firstField = false;
                sqlBuilder.appendName(versionField.getName(), sqlSB).append("=?");
            }

            for (int a = 0, size = fieldNames.length; a < size; a++) {
                var fieldName = fieldNames[a];
                if (fieldName == null) {
                    // Value not specified
                    continue;
                }
                if (!firstField) {
                    sqlSB.append(',');
                } else {
                    firstField = false;
                }
                sqlBuilder.appendName(fieldName, sqlSB).append("=?");
            }
            var updatedOnField = metaData.getUpdatedOnField();
            if (updatedOnField != null) {
                if (!firstField) {
                    sqlSB.append(',');
                }
                firstField = false;
                sqlBuilder.appendName(updatedOnField.getName(), sqlSB).append("=?");
            }
            var updatedByField = metaData.getUpdatedByField();
            if (updatedByField != null) {
                if (!firstField) {
                    sqlSB.append(',');
                }
                firstField = false;
                sqlBuilder.appendName(updatedByField.getName(), sqlSB).append("=?");
            }
            sqlSB.append(" WHERE ");
            firstField = true;
            for (var idField : idFields) {
                if (firstField) {
                    firstField = false;
                } else {
                    sqlSB.append(" AND ");
                }
                sqlBuilder.appendName(idField.getName(), sqlSB).append("=?");
            }
            if (connectionDialect.useVersionOnOptimisticUpdate() && versionField != null) {
                sqlSB.append(" AND ");
                sqlBuilder.appendName(versionField.getName(), sqlSB).append("=?");
            }
            if (log.isDebugEnabled()) {
                log.debug("prepare: " + sqlSB);
            }

            return connection.prepareStatement(sqlSB.toString());
        } catch (Throwable e) {
            throw RuntimeExceptionUtil.mask(e, sqlSB.toString());
        } finally {
            tlObjectCollector.dispose(sqlSB);
        }
    }

    protected PreparedStatement createDeleteStatementWithIn() {
        if (deleteStmt != null) {
            return deleteStmt;
        }
        var metaData = getMetaData();
        var idFields = metaData.getIdFields();

        var tlObjectCollector = objectCollector.getCurrent();
        var sqlSB = tlObjectCollector.create(AppendableStringBuilder.class);
        try {
            sqlSB.append("DELETE FROM ");
            sqlBuilder.appendName(metaData.getFullqualifiedEscapedName(), sqlSB).append(" WHERE ");
            var firstField = true;
            for (var idField : idFields) {
                if (firstField) {
                    firstField = false;
                } else {
                    sqlSB.append(" AND ");
                }
                sqlBuilder.appendName(idField.getName(), sqlSB);
                sqlSB.append("=?");
            }
            deleteStmt = connection.prepareStatement(sqlSB.toString());
            return deleteStmt;
        } catch (Throwable e) {
            throw RuntimeExceptionUtil.mask(e, sqlSB.toString());
        } finally {
            tlObjectCollector.dispose(sqlSB);
        }
    }

    protected String generateNamesKey(ILinkedMap<IFieldMetaData, Object> puis, String[] fieldNames, Object[] values) {
        var tlObjectCollector = objectCollector.getCurrent();
        var namesKeySB = tlObjectCollector.create(StringBuilder.class);
        try {
            for (var entry : puis) {
                var field = entry.getKey();
                var newValue = entry.getValue();
                if (newValue == null && java.sql.Array.class.isAssignableFrom(field.getFieldType())) {
                    newValue = Array.newInstance(field.getFieldSubType(), 0);
                }
                var convertedValue = connectionDialect.convertToFieldType(field, newValue);
                int fieldIndex = field.getIndexOnTable();
                values[fieldIndex] = convertedValue;
                fieldNames[fieldIndex] = field.getName();
            }
            for (int a = 0, size = fieldNames.length; a < size; a++) {
                var fieldName = fieldNames[a];
                if (fieldName == null) {
                    continue;
                }
                if (namesKeySB.length() > 0) {
                    namesKeySB.append('#');
                }
                namesKeySB.append(fieldName);
            }
            return namesKeySB.toString();
        } finally {
            tlObjectCollector.dispose(namesKeySB);
        }
    }
}
