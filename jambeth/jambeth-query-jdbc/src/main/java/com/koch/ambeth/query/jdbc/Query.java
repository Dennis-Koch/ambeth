package com.koch.ambeth.query.jdbc;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.EntityCursor;
import com.koch.ambeth.persistence.IServiceUtil;
import com.koch.ambeth.persistence.Table;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilderExtension;
import com.koch.ambeth.query.IQueryIntern;
import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.query.ISubQuery;
import com.koch.ambeth.query.ParameterizedQuery;
import com.koch.ambeth.query.jdbc.sql.ITableAliasHolder;
import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IEntityCursor;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import lombok.SneakyThrows;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

@PersistenceContext
public class Query<T> implements IQuery<T>, IQueryIntern<T>, ISubQuery<T> {
    @Autowired
    protected IConversionHelper conversionHelper;

    @Autowired
    protected IDatabase database;

    @Autowired
    protected IThreadLocalObjectCollector objectCollector;

    @Autowired
    protected IServiceUtil serviceUtil;

    @Autowired
    protected IStringQuery stringQuery;

    @Autowired
    protected ITableAliasHolder tableAliasHolder;

    @Autowired
    protected ITransaction transaction;

    @Property
    protected Class<T> entityType;

    @Property(defaultValue = "false")
    protected boolean containsSubQuery;

    @Property
    protected IOperand rootOperand;

    @Property
    protected IOperand[] groupByOperands;

    @Property
    protected IOperand[] orderByOperands;

    @Property
    protected IOperand limitOperand;

    @Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
    protected boolean securityActive;

    @Property
    protected IOperand[] selectOperands;

    @Property
    protected List<Class<?>> relatedEntityTypes;

    @Property
    protected IQueryIntern<T> transactionalQuery;

    @Property
    protected IQueryBuilderExtension[] queryBuilderExtensions;

    protected IQueryKey queryKey;

    @Override
    public Class<T> getEntityType() {
        return this.entityType;
    }

    @Override
    public void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes) {
        for (int a = 0, size = this.relatedEntityTypes.size(); a < size; a++) {
            relatedEntityTypes.add(this.relatedEntityTypes.get(a));
        }
    }

    @Override
    public IQueryKey getQueryKey(Map<Object, Object> nameToValueMap) {
        if (this.queryKey == null) {
            IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

            IMap<Object, Object> tempNameToValueMap = null;
            if (nameToValueMap == null) {
                tempNameToValueMap = new HashMap<>();
                nameToValueMap = tempNameToValueMap;
            }

            String[] sqlParts = getSqlParts(nameToValueMap, null, null);
            StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
            try {
                for (int i = 0; i < sqlParts.length; i++) {
                    sb.append('#');
                    if (sqlParts[i] != null) {
                        sb.append(sqlParts[i]);
                    }
                }
                this.queryKey = new StringQueryKey(this.entityType, sb.toString());
            } finally {
                tlObjectCollector.dispose(sb);
            }
        }

        return this.queryKey;
    }

    protected Object buildCursor(Map<Object, Object> nameToValueMapSrc, RetrievalType retrievalType, int limitValue, boolean retrieveAlternateIds) {
        if (!transaction.isActive()) {
            if (transaction.initializeTransactionIfLazyActive() && !transaction.isActive()) {
                throw new IllegalStateException(IQuery.class.getSimpleName() + ".retrieveAsVersions() can only be called from within a @" + PersistenceContext.class.getSimpleName() +
                        ". This is because the IVersionCursor may " + "hold active resources of underlying databases");
            }
        }
        var nameToValueMap = new HashMap<>();
        if (nameToValueMapSrc != null) {
            nameToValueMap.putAll(nameToValueMapSrc);
        }
        if (containsSubQuery) {
            nameToValueMap.put(QueryConstants.USE_TABLE_ALIAS, Boolean.TRUE);
        }
        if (limitValue > 0) {
            nameToValueMap.put(QueryConstants.LIMIT_VALUE, Integer.valueOf(limitValue));
        } else {
            nameToValueMap.put(QueryConstants.LIMIT_VALUE, null);
        }

        var additionalSelectColumnList = new ArrayList<String>();
        var parameters = new ArrayList<>();
        var pagingSizeObject = nameToValueMap.get(QueryConstants.PAGING_SIZE_OBJECT);

        var sqlParts = getSqlParts(nameToValueMap, parameters, additionalSelectColumnList);
        var joinSql = sqlParts[0];
        var whereSql = sqlParts[1];
        var orderBySql = sqlParts[2];
        var limitSql = sqlParts[3];

        var tableAlias = (stringQuery.isJoinQuery() || containsSubQuery) ? tableAliasHolder.getTableAlias() : null;

        var table = (Table) this.database.getTableByType(this.entityType);

        if (RetrievalType.DATA.equals(retrievalType)) {
            fillAdditionalFieldsSQL(additionalSelectColumnList, null, nameToValueMap, stringQuery.isJoinQuery(), parameters);
        }
        if (pagingSizeObject == null) {
            if (RetrievalType.VERSION.equals(retrievalType)) {
                return table.selectVersionJoin(additionalSelectColumnList, joinSql, whereSql, orderBySql, limitSql, parameters, tableAlias, retrieveAlternateIds);
            } else if (RetrievalType.COUNT.equals(retrievalType)) {
                return Long.valueOf(table.selectCountJoin(joinSql, whereSql, orderBySql, parameters, tableAlias));
            }
            return table.selectDataJoin(additionalSelectColumnList, joinSql, whereSql, orderBySql, limitSql, parameters, tableAlias);
        } else if (RetrievalType.COUNT.equals(retrievalType)) {
            return Long.valueOf(table.selectCountJoin(joinSql, whereSql, orderBySql, parameters, tableAlias));
        }
        var pagingIndexObject = nameToValueMap.get(QueryConstants.PAGING_INDEX_OBJECT);

        var pagingLimit = conversionHelper.convertValueToType(Integer.TYPE, pagingSizeObject);
        var pagingOffset = conversionHelper.convertValueToType(Integer.TYPE, pagingIndexObject);

        if (RetrievalType.VERSION.equals(retrievalType)) {
            return table.selectVersionPaging(additionalSelectColumnList, joinSql, whereSql, orderBySql, limitSql, pagingOffset, pagingLimit, parameters, tableAlias, retrieveAlternateIds);
        }
        return table.selectDataPaging(additionalSelectColumnList, joinSql, whereSql, orderBySql, limitSql, pagingOffset, pagingLimit, parameters);
    }

    @Override
    public String[] getSqlParts(Map<Object, Object> nameToValueMap, List<Object> parameters, List<String> additionalSelectColumnList) {
        var tlObjectCollector = objectCollector.getCurrent();

        var useTableAliasOriginal = nameToValueMap.get(QueryConstants.USE_TABLE_ALIAS);
        if (containsSubQuery) {
            nameToValueMap.put(QueryConstants.USE_TABLE_ALIAS, Boolean.TRUE);
        }
        for (IQueryBuilderExtension queryBuilderExtension : queryBuilderExtensions) {
            queryBuilderExtension.applyOnQuery(nameToValueMap, parameters, additionalSelectColumnList);
        }
        String joinSql, whereSql;
        var joinQuery = stringQuery.isJoinQuery();
        if (!joinQuery) {
            joinSql = null;
            whereSql = stringQuery.fillQuery(nameToValueMap, parameters);
        } else {
            String[] sqlParts = stringQuery.fillJoinQuery(nameToValueMap, parameters);

            joinSql = sqlParts[0];
            whereSql = sqlParts[1];
        }
        var tempSB = tlObjectCollector.create(AppendableStringBuilder.class);
        try {
            fillOrderBySQL(additionalSelectColumnList, tempSB, nameToValueMap, joinQuery, parameters);
            String orderBySql = tempSB.length() > 0 ? tempSB.toString() : null;
            tempSB.reset();
            fillLimitSQL(additionalSelectColumnList, tempSB, nameToValueMap, joinQuery, parameters);
            String limitSql = tempSB.length() > 0 ? tempSB.toString() : null;
            String[] sqlParts = { joinSql, whereSql, orderBySql, limitSql };
            return sqlParts;
        } finally {
            nameToValueMap.put(QueryConstants.USE_TABLE_ALIAS, useTableAliasOriginal);
            tlObjectCollector.dispose(tempSB);
        }
    }

    protected void fillOrderBySQL(List<String> additionalSelectColumnList, IAppendable orderBySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) {
        if (orderByOperands.length == 0 && groupByOperands.length == 0) {
            return;
        }
        nameToValueMap.put(QueryConstants.FIRST_ORDER_BY_STATE, Boolean.TRUE);
        nameToValueMap.put(QueryConstants.ADDITIONAL_SELECT_SQL_SB, additionalSelectColumnList);
        try {
            var firstGroupBy = true;
            for (int a = 0, size = groupByOperands.length; a < size; a++) {
                if (firstGroupBy) {
                    orderBySB.append("GROUP BY ");
                    firstGroupBy = false;
                } else {
                    orderBySB.append(',');
                }
                groupByOperands[a].expandQuery(orderBySB, nameToValueMap, joinQuery, parameters);
            }
            for (int a = 0, size = orderByOperands.length; a < size; a++) {
                orderByOperands[a].expandQuery(orderBySB, nameToValueMap, joinQuery, parameters);
            }
        } finally {
            nameToValueMap.remove(QueryConstants.ADDITIONAL_SELECT_SQL_SB);
            nameToValueMap.remove(QueryConstants.FIRST_ORDER_BY_STATE);
        }
    }

    protected void fillLimitSQL(List<String> additionalSelectColumnList, IAppendable limitSB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) {
        if (limitOperand == null) {
            return;
        }
        limitOperand.expandQuery(limitSB, nameToValueMap, joinQuery, parameters);
    }

    protected void fillAdditionalFieldsSQL(List<String> additionalSelectColumnList, IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) {
        if (selectOperands.length == 0) {
            return;
        }
        nameToValueMap.put(QueryConstants.ADDITIONAL_SELECT_SQL_SB, additionalSelectColumnList);
        try {
            for (int a = 0, size = selectOperands.length; a < size; a++) {
                selectOperands[a].expandQuery(querySB, nameToValueMap, joinQuery, parameters);
            }
        } finally {
            nameToValueMap.remove(QueryConstants.ADDITIONAL_SELECT_SQL_SB);
        }
    }

    @Override
    public IVersionCursor retrieveAsVersions() {
        return (IVersionCursor) buildCursor(null, RetrievalType.VERSION, 0, true);
    }

    @Override
    public IVersionCursor retrieveAsVersions(boolean retrieveAlternateIds) {
        return (IVersionCursor) buildCursor(null, RetrievalType.VERSION, 0, retrieveAlternateIds);
    }

    protected IVersionCursor retrieveAsVersions(Map<Object, Object> nameToValueMap) {
        return (IVersionCursor) buildCursor(nameToValueMap, RetrievalType.VERSION, 0, true);
    }

    @Override
    public IVersionCursor retrieveAsVersions(Map<Object, Object> nameToValueMap, boolean retrieveAlternateIds) {
        return (IVersionCursor) buildCursor(nameToValueMap, RetrievalType.VERSION, 0, retrieveAlternateIds);
    }

    @Override
    public List<IObjRef> retrieveAsObjRefs(Map<Object, Object> nameToValueMap, int idIndex) {
        return serviceUtil.loadObjRefs(entityType, idIndex, retrieveAsVersions(nameToValueMap, ObjRef.PRIMARY_KEY_INDEX != idIndex));
    }

    @Override
    public List<IObjRef> retrieveAsObjRefs(int idIndex) {
        return serviceUtil.loadObjRefs(entityType, idIndex, retrieveAsVersions(null, ObjRef.PRIMARY_KEY_INDEX != idIndex));
    }

    @Override
    public IDataCursor retrieveAsData() {
        return (IDataCursor) buildCursor(null, RetrievalType.DATA, 0, false);
    }

    @Override
    public IDataCursor retrieveAsData(Map<Object, Object> nameToValueMap) {
        return (IDataCursor) buildCursor(nameToValueMap, RetrievalType.DATA, 0, false);
    }

    @Override
    public IEntityCursor<T> retrieveAsCursor() {
        return retrieveAsCursor(null);
    }

    @Override
    public IEntityCursor<T> retrieveAsCursor(Map<Object, Object> nameToValueMap) {
        var cursor = retrieveAsVersions(nameToValueMap);
        return new EntityCursor<>(cursor, this.entityType, this.serviceUtil);
    }

    @Override
    public List<T> retrieve() {
        return retrieve(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> retrieve(Map<Object, Object> nameToValueMap) {
        var cursor = retrieveAsVersions(nameToValueMap);
        if (cursor == null) {
            return (List<T>) EmptyList.instance;
        }
        var resultList = new ArrayList<T>();
        serviceUtil.loadObjectsIntoCollection(resultList, entityType, cursor);
        return resultList;
    }

    @Override
    public T retrieveSingle() {
        var result = retrieve();
        if (result == null || result.isEmpty()) {
            return null;
        }
        if (result.size() != 1) {
            throw new IllegalStateException("Query result is not unique: " + result.size());
        }
        return result.get(0);
    }

    @Override
    public long count() {
        return count(null);
    }

    @Override
    public long count(Map<Object, Object> paramNameToValueMap) {
        return ((Long) buildCursor(paramNameToValueMap, RetrievalType.COUNT, 0, false)).longValue();
    }

    @Override
    public boolean isEmpty() {
        return isEmpty(null);
    }

    @Override
    public boolean isEmpty(Map<Object, Object> paramNameToValueMap) {
        var versionCursor = (IVersionCursor) buildCursor(paramNameToValueMap, RetrievalType.VERSION, 1, false);
        try {
            var iter = versionCursor.iterator();
            return !iter.hasNext();
        } finally {
            versionCursor.dispose();
        }
    }

    @Override
    public IQuery<T> param(Object paramKey, Object param) {
        var parameterizedQuery = new ParameterizedQuery<>(transactionalQuery);
        return parameterizedQuery.param(paramKey, param);
    }

    @Override
    public String getMainTableAlias() {
        return tableAliasHolder.getTableAlias();
    }

    @Override
    public void setMainTableAlias(String alias) {
        tableAliasHolder.setTableAlias(alias);
    }

    @SneakyThrows
    @Override
    public void dispose() {
        for (var field : ReflectUtil.getDeclaredFieldsInHierarchy(getClass())) {
            var modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || field.getType().isPrimitive()) {
                continue;
            }
            field.set(this, null);
        }
    }
}
