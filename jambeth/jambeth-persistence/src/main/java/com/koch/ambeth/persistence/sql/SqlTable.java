package com.koch.ambeth.persistence.sql;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.persistence.IPersistenceHelper;
import com.koch.ambeth.persistence.Table;
import com.koch.ambeth.persistence.api.IContextProvider;
import com.koch.ambeth.persistence.api.ICursor;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyMap;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IdentityHashMap;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class SqlTable extends Table {
    public static final Pattern quotesPattern = Pattern.compile("\"", Pattern.LITERAL);

    @Autowired
    protected IContextProvider contextProvider;

    @Autowired
    protected IConversionHelper conversionHelper;

    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Autowired
    protected IPersistenceHelper persistenceHelper;

    @Autowired
    protected ISqlConnection sqlConnection;

    @Autowired
    protected ISqlBuilder sqlBuilder;

    @Override
    public void delete(List<IObjRef> oris) {
        var conversionHelper = this.conversionHelper;
        var sqlBuilder = this.sqlBuilder;
        var objectCollector = this.objectCollector.getCurrent();
        var sb = objectCollector.create(AppendableStringBuilder.class);
        var whereSqls = new CharSequence[oris.size()];
        try {
            var metaData = getMetaData();
            var idField = metaData.getIdField();
            var idFieldName = idField.getName();
            var versionField = metaData.getVersionField();
            var idFieldType = idField.getFieldType();
            String versionFieldName = null;
            Class<?> versionFieldType = null;
            if (versionField != null) {
                versionFieldName = versionField.getName();
                versionFieldType = versionField.getFieldType();
            }
            for (int i = oris.size(); i-- > 0; ) {
                var ori = oris.get(i);
                var id = conversionHelper.convertValueToType(idFieldType, ori.getId());
                sqlBuilder.appendNameValue(idFieldName, id, sb);
                var version = ori.getVersion();
                if (version != null && versionField != null) {
                    version = conversionHelper.convertValueToType(versionFieldType, version);
                    sb.append(" AND ");
                    sqlBuilder.appendNameValue(versionFieldName, version, sb);
                }
                whereSqls[i] = sb.toString();
                sb.reset();
            }
            sqlConnection.queueDelete(getMetaData().getFullqualifiedEscapedName(), whereSqls);
        } finally {
            objectCollector.dispose(sb);
        }
    }

    @Override
    public void deleteAll() {
        sqlConnection.queueDeleteAll(getMetaData().getFullqualifiedEscapedName());
    }

    @Override
    public ICursor selectValues(int idIndex, List<?> alternateIds) {
        var tableMetaData = getMetaData();
        var objectCollector = this.objectCollector.getCurrent();
        var selectSB = objectCollector.create(AppendableStringBuilder.class);
        try {
            var selectState = new SelectState(selectSB);
            appendSelectPrimaryIds(tableMetaData, null, false, null, selectState);
            appendSelectVersion(tableMetaData, null, selectState);
            appendAllFields(tableMetaData, null, selectState);
            appendAlternateIds(tableMetaData, null, null, selectState);

            var alternateIdFields = tableMetaData.getIdFieldsByAlternateIdIndex(idIndex);
            var idContainer = IdContainerImpl.ofIdIndex(alternateIdFields, alternateIds, tableMetaData.getEntityType(), entityMetaDataProvider);

            var resultSet = sqlConnection.createResultSet(getMetaData().getFullqualifiedEscapedName(), selectSB, null, null, idContainer);
            var cursor = new ResultSetCursor();
            cursor.setVersionIndex(selectState.versionIndex);
            cursor.setCompositeIdCount(idContainer.getDecomposedIdCount());
            cursor.setResultSetItemToAlternateIdConverter(selectState.resultSetItemToAlternateIdConverter);
            cursor.setResultSet(resultSet);
            cursor.setFields(selectState.cursorFields.toArray(IFieldMetaData[]::new));
            cursor.setFieldToColumnIndexMap(selectState.fieldToColumnIndexMap);
            cursor.afterPropertiesSet();
            return cursor;
        } finally {
            objectCollector.dispose(selectSB);
        }
    }

    @Override
    public IVersionCursor selectVersion(int idIndex, List<?> ids) {
        var tableMetaData = getMetaData();
        var objectCollector = this.objectCollector.getCurrent();
        var selectSB = objectCollector.create(AppendableStringBuilder.class);
        try {
            var selectState = new SelectState(selectSB);
            appendSelectPrimaryIds(tableMetaData, null, false, null, selectState);
            appendSelectVersion(tableMetaData, null, selectState);

            ISqlConnection.IdContainer idContainer;
            if (idIndex == IObjRef.PRIMARY_KEY_INDEX) {
                idContainer = IdContainerImpl.ofIdIndex(tableMetaData.getIdFields(), ids, tableMetaData.getEntityType(), entityMetaDataProvider);
            } else {
                var alternateIdFields = tableMetaData.getIdFieldsByAlternateIdIndex(idIndex);
                idContainer = IdContainerImpl.ofIdIndex(alternateIdFields, ids, tableMetaData.getEntityType(), entityMetaDataProvider);

                for (var idFieldName : idContainer.getFieldNames()) {
                    if (selectState.columnIndex >= 0) {
                        selectSB.append(',');
                    }
                    sqlBuilder.appendName(idFieldName, selectSB);
                    selectState.columnIndex++;
                }
            }

            var versionCursor = new ResultSetVersionCursor();
            versionCursor.setCompositeIdCount(idContainer.getDecomposedIdCount());
            versionCursor.setVersionIndex(selectState.versionIndex);
            versionCursor.setResultSet(sqlConnection.createResultSet(getMetaData().getFullqualifiedEscapedName(), selectSB, null, null, idContainer));
            versionCursor.afterPropertiesSet();
            return versionCursor;
        } finally {
            objectCollector.dispose(selectSB);
        }
    }

    @Override
    public IVersionCursor selectVersionWhere(List<String> additionalSelectColumnList, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql, List<Object> parameters) {
        return selectVersionJoin(additionalSelectColumnList, null, whereSql, orderBySql, limitSql, parameters);
    }

    @Override
    public IVersionCursor selectVersionJoin(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
            List<Object> parameters) {
        var join = joinSql != null && joinSql.length() > 0;
        var tableAlias = join ? "A" : null;
        return selectVersionJoin(additionalSelectColumnList, joinSql, whereSql, orderBySql, limitSql, parameters, tableAlias, true);
    }

    @Override
    public IVersionCursor selectVersionJoin(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
            List<Object> parameters, String tableAlias, boolean retrieveAlternateIds) {
        var tableMetaData = getMetaData();
        var objectCollector = this.objectCollector.getCurrent();
        var selectSB = objectCollector.create(AppendableStringBuilder.class);
        HashSet<String> additionalSelectColumnSet = null;
        try {
            if (additionalSelectColumnList != null) {
                additionalSelectColumnSet = new HashSet<>();
                for (int a = additionalSelectColumnList.size(); a-- > 0; ) {
                    var additionalSelectColumn = additionalSelectColumnList.get(a);
                    // additional columns are already escaped
                    additionalSelectColumn = quotesPattern.matcher(additionalSelectColumn).replaceAll("");
                    additionalSelectColumnSet.add(additionalSelectColumn);
                }
            }

            var selectState = new SelectState(selectSB);
            appendSelectPrimaryIds(tableMetaData, tableAlias, retrieveAlternateIds, additionalSelectColumnList, selectState);
            appendSelectVersion(tableMetaData, tableAlias, selectState);
            if (retrieveAlternateIds) {
                appendAlternateIds(tableMetaData, tableAlias, additionalSelectColumnList, selectState);
            }
            if (additionalSelectColumnSet != null && !additionalSelectColumnSet.isEmpty()) {
                for (var additionalFieldName : additionalSelectColumnSet) {
                    if (selectState.columnIndex >= 0) {
                        selectSB.append(',');
                    }
                    var schemaAndTableName = sqlBuilder.getSchemaAndTableName(additionalFieldName);
                    if (schemaAndTableName[0] != null) {
                        selectSB.append(schemaAndTableName[0]).append('.');
                    }
                    // JH 2015-04-28: Field names have to be escaped. Fields from orderBy are processed here.
                    sqlBuilder.appendName(schemaAndTableName[1], selectSB);
                    selectState.columnIndex++;
                }
            }

            var fqTableName = getMetaData().getFullqualifiedEscapedName();
            var selectResult = sqlConnection.selectFields(fqTableName, selectSB, joinSql, whereSql, orderBySql, limitSql, parameters, tableAlias);

            if (retrieveAlternateIds) {
                var versionCursor = new ResultSetVersionCursor();
                versionCursor.setCompositeIdCount(tableMetaData.getIdFields().length);
                versionCursor.setVersionIndex(selectState.versionIndex);
                versionCursor.setResultSetItemToAlternateIdConverter(selectState.resultSetItemToAlternateIdConverter);
                versionCursor.setResultSet(selectResult);
                versionCursor.afterPropertiesSet();

                return versionCursor;
            }
            var versionCursor = new ResultSetPkVersionCursor();
            versionCursor.setCompositeIdCount(tableMetaData.getIdFields().length);
            versionCursor.setVersionIndex(selectState.versionIndex);
            versionCursor.setResultSet(selectResult);
            versionCursor.afterPropertiesSet();

            return versionCursor;
        } finally {
            objectCollector.dispose(selectSB);
        }
    }

    @Override
    public long selectCountJoin(CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, List<Object> parameters, String tableAlias) {
        var resultSet = sqlConnection.selectFields(getMetaData().getFullqualifiedEscapedName(), "COUNT(*)", joinSql, whereSql, null, null, parameters, tableAlias);
        try {
            var resultSetIter = resultSet.iterator();
            if (!resultSetIter.hasNext()) {
                return 0;
            }
            var countValues = resultSetIter.next();
            return ((Number) countValues[0]).longValue();
        } finally {
            resultSet.dispose();
        }
    }

    @Override
    public IVersionCursor selectVersionPaging(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql, int offset,
            int length, List<Object> parameters) {
        var join = joinSql != null && joinSql.length() > 0;
        var tableAlias = join ? "A" : null;
        return selectVersionPaging(additionalSelectColumnList, joinSql, whereSql, orderBySql, limitSql, offset, length, parameters, tableAlias, true);
    }

    @Override
    public IVersionCursor selectVersionPaging(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql, int offset,
            int length, List<Object> parameters, String tableAlias, boolean retrieveAlternateIds) {
        var tableMetaData = getMetaData();
        var objectCollector = this.objectCollector.getCurrent();
        var selectSB = objectCollector.create(AppendableStringBuilder.class);
        try {
            var selectState = new SelectState(selectSB);
            appendSelectPrimaryIds(tableMetaData, tableAlias, retrieveAlternateIds, additionalSelectColumnList, selectState);
            appendSelectVersion(tableMetaData, tableAlias, selectState);
            if (retrieveAlternateIds) {
                appendAlternateIds(tableMetaData, tableAlias, additionalSelectColumnList, selectState);
            }

            var fqTableName = getMetaData().getFullqualifiedEscapedName();
            var selectResult = sqlConnection.selectFields(fqTableName, selectSB, joinSql, whereSql, additionalSelectColumnList, orderBySql, limitSql, offset, length, parameters, tableAlias);

            if (retrieveAlternateIds) {
                var versionCursor = new ResultSetVersionCursor();
                versionCursor.setCompositeIdCount(tableMetaData.getIdFields().length);
                versionCursor.setVersionIndex(selectState.versionIndex);
                versionCursor.setResultSetItemToAlternateIdConverter(selectState.resultSetItemToAlternateIdConverter);
                versionCursor.setResultSet(selectResult);
                versionCursor.afterPropertiesSet();

                return versionCursor;
            }
            var versionCursor = new ResultSetPkVersionCursor();
            versionCursor.setCompositeIdCount(tableMetaData.getIdFields().length);
            versionCursor.setVersionIndex(selectState.versionIndex);
            versionCursor.setResultSet(selectResult);
            versionCursor.afterPropertiesSet();

            return versionCursor;
        } finally {
            objectCollector.dispose(selectSB);
        }
    }

    protected void appendAlternateIds(ITableMetaData tableMetaData, String tableAlias, List<String> additionalSelectColumnList, SelectState selectState) {
        var alternateIdsFields = tableMetaData.getAlternateIdsFields();

        Function<Object[], Object[]>[] resultSetItemToAlternateIdConverter = new Function[alternateIdsFields.length];
        for (int idIndex = alternateIdsFields.length; idIndex-- > 0; ) {
            var alternateIdFields = alternateIdsFields[idIndex];
            resultSetItemToAlternateIdConverter[idIndex] = buildResultSetToAlternateIdConverter(alternateIdFields, additionalSelectColumnList, tableAlias, selectState);
        }
        selectState.resultSetItemToAlternateIdConverter = resultSetItemToAlternateIdConverter;
    }

    protected void appendAllFields(ITableMetaData tableMetaData, String tableAlias, SelectState selectState) {
        var selectSB = selectState.selectSB;
        var fieldToColumnIndexMap = selectState.getOrCreateFieldToColumnIndexMap();
        var allFields = tableMetaData.getAllFields();
        var cursorFields = new ArrayList<IFieldMetaData>(allFields.size());
        for (var field : tableMetaData.getAllFields()) {
            var member = field.getMember();
            if (member == null) {
                // Ignore fields which can not be loaded into entities
                continue;
            }
            cursorFields.add(field);
            var existingColumnIndex = fieldToColumnIndexMap.get(field);
            if (existingColumnIndex == null) {
                if (selectState.columnIndex >= 0) {
                    selectSB.append(',');
                }
                if (tableAlias != null) {
                    selectSB.append(tableAlias).append(".");
                }
                var fieldName = field.getName();
                sqlBuilder.appendName(fieldName, selectSB);
                selectState.columnIndex++;
                fieldToColumnIndexMap.put(field, selectState.columnIndex);
            }
        }
        selectState.cursorFields = cursorFields;
    }

    protected Function<Object[], Object[]> buildResultSetToAlternateIdConverter(IFieldMetaData[] alternateIdFields, List<String> additionalSelectColumnList, String tableAlias,
            SelectState selectState) {
        var cursorAccessors = new ArrayList<Function<Object[], Object>>(alternateIdFields.length);
        var selectSB = selectState.selectSB;
        var fieldToColumnIndexMap = selectState.getOrCreateFieldToColumnIndexMap();
        for (var field : alternateIdFields) {
            var member = field.getMember();
            if (member == null) {
                // Ignore fields which can not be loaded into entities
                return null;
            }
            var existingColumnIndex = fieldToColumnIndexMap.get(field);
            if (existingColumnIndex == null) {
                if (selectState.columnIndex >= 0) {
                    selectSB.append(',');
                }
                if (tableAlias != null) {
                    selectSB.append(tableAlias).append(".");
                }
                var fieldName = field.getName();
                sqlBuilder.appendName(fieldName, selectSB);
                selectState.columnIndex++;
                fieldToColumnIndexMap.put(field, selectState.columnIndex);
                existingColumnIndex = selectState.columnIndex;

                // When ordering by an AK it is selected twice. So one needs an alias.
                if (additionalSelectColumnList.contains("\"" + fieldName + "\"")) {
                    selectSB.append(" AS AK").append(Integer.toString(fieldToColumnIndexMap.size()));
                }
            }
            var fColumnIndex = existingColumnIndex;
            cursorAccessors.add(current -> current[fColumnIndex]);
        }
        if (cursorAccessors.isEmpty()) {
            return null;
        }
        if (cursorAccessors.size() == 1) {
            var cursorAccessor = cursorAccessors.get(0);
            var sharedArray = new Object[1];
            return current -> {
                sharedArray[0] = cursorAccessor.apply(current);
                return sharedArray;
            };
        }
        Function<Object[], Object>[] cursorAccessorsArray = cursorAccessors.toArray(Function[]::new);
        var sharedArray = new Object[cursorAccessorsArray.length];
        return current -> {
            for (int a = cursorAccessorsArray.length; a-- > 0; ) {
                sharedArray[a] = cursorAccessorsArray[a].apply(current);
            }
            return sharedArray;
        };
    }

    @Override
    public IDataCursor selectDataJoin(List<String> selectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitBySql, List<Object> parameters) {
        boolean join = joinSql != null && joinSql.length() > 0;
        String tableAlias = join ? "A" : null;
        return selectDataJoin(selectColumnList, joinSql, whereSql, orderBySql, limitBySql, parameters, tableAlias);
    }

    @Override
    public IDataCursor selectDataJoin(List<String> selectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitBySql, List<Object> parameters,
            String tableAlias) {
        var objectCollector = this.objectCollector.getCurrent();
        var propertyToColIndexMap = new HashMap<String, Integer>();
        var selectSB = objectCollector.create(StringBuilder.class);
        try {
            for (int a = 0, size = selectColumnList.size(); a < size; a++) {
                var additionalFieldName = selectColumnList.get(a);
                // additionaFieldName is expected to be already escaped at this point. No need to double
                // escape
                if (a > 0) {
                    selectSB.append(',');
                }
                selectSB.append(additionalFieldName);
                propertyToColIndexMap.put(selectColumnList.get(a), Integer.valueOf(a));
            }
            var dataCursor = new ResultSetDataCursor();
            dataCursor.setPropertyToColIndexMap(propertyToColIndexMap);
            dataCursor.setResultSet(sqlConnection.selectFields(getMetaData().getFullqualifiedEscapedName(), selectSB, joinSql, whereSql, orderBySql, limitBySql, parameters, tableAlias));
            dataCursor.afterPropertiesSet();
            return dataCursor;
        } finally {
            objectCollector.dispose(selectSB);
        }
    }

    @Override
    public IDataCursor selectDataPaging(List<String> selectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql, int offset, int length,
            List<Object> parameters) {
        var size = selectColumnList.size();
        var propertyToColIndexMap = size > 0 ? HashMap.<String, Integer>create(size) : EmptyMap.<String, Integer>emptyMap();
        for (int a = 0; a < size; a++) {
            propertyToColIndexMap.put(selectColumnList.get(a), Integer.valueOf(a));
        }
        var dataCursor = new ResultSetDataCursor();
        dataCursor.setPropertyToColIndexMap(propertyToColIndexMap);
        dataCursor.setResultSet(sqlConnection.selectFields(getMetaData().getFullqualifiedEscapedName(), "", joinSql, whereSql, selectColumnList, orderBySql, limitSql, offset, length, parameters));
        dataCursor.afterPropertiesSet();
        return dataCursor;
    }

    @Override
    public IVersionCursor selectAll() {
        var tableMetaData = getMetaData();
        var objectCollector = this.objectCollector.getCurrent();
        var selectSB = objectCollector.create(AppendableStringBuilder.class);
        try {
            var selectState = new SelectState(selectSB);
            appendSelectPrimaryIds(tableMetaData, null, false, null, selectState);
            appendSelectVersion(tableMetaData, null, selectState);
            var versionCursor = new ResultSetVersionCursor();
            versionCursor.setVersionIndex(selectState.versionIndex);
            versionCursor.setResultSet(sqlConnection.selectFields(getMetaData().getFullqualifiedEscapedName(), selectSB, null, null, null, null));
            versionCursor.afterPropertiesSet();
            return versionCursor;
        } finally {
            objectCollector.dispose(selectSB);
        }
    }

    protected void appendSelectPrimaryIds(ITableMetaData tableMetaData, String tableAlias, boolean retrieveAlternateIds, List<String> additionalSelectColumnList, SelectState selectState) {
        var selectSB = selectState.selectSB;
        var idFields = tableMetaData.getIdFields();
        for (int pkIndex = 0, size = idFields.length; pkIndex < size; pkIndex++) {
            var primaryIdFieldName = idFields[pkIndex].getName();
            if (selectState.columnIndex >= 0) {
                selectSB.append(',');
            }
            if (tableAlias != null) {
                selectSB.append(tableAlias).append(".");
            }
            sqlBuilder.appendName(primaryIdFieldName, selectSB);
            selectState.columnIndex++;
            if (retrieveAlternateIds && additionalSelectColumnList != null && additionalSelectColumnList.contains("\"" + primaryIdFieldName + "\"")) {
                selectSB.append(" AS PK").append(pkIndex);
            }
        }
    }

    protected void appendSelectVersion(ITableMetaData tableMetaData, String tableAlias, SelectState selectState) {
        var versionField = tableMetaData.getVersionField();
        if (versionField == null) {
            return;
        }
        var selectSB = selectState.selectSB;
        if (selectState.columnIndex >= 0) {
            selectSB.append(',');
        }
        if (tableAlias != null) {
            selectSB.append(tableAlias).append(".");
        }
        sqlBuilder.appendName(versionField.getName(), selectSB);
        selectState.columnIndex++;
        selectState.versionIndex = selectState.columnIndex;
    }

    @RequiredArgsConstructor
    public static class SelectState {

        final AppendableStringBuilder selectSB;
        IdentityHashMap<IFieldMetaData, Integer> fieldToColumnIndexMap;
        int columnIndex = -1;

        int versionIndex = -1;

        Function<Object[], Object[]>[] resultSetItemToAlternateIdConverter;

        List<IFieldMetaData> cursorFields;

        public IdentityHashMap<IFieldMetaData, Integer> getOrCreateFieldToColumnIndexMap() {
            if (fieldToColumnIndexMap == null) {
                fieldToColumnIndexMap = new IdentityHashMap<>();
            }
            return fieldToColumnIndexMap;
        }
    }
}
