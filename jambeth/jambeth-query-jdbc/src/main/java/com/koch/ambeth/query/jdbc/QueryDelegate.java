package com.koch.ambeth.query.jdbc;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryIntern;
import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IEntityCursor;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

public class QueryDelegate<T> implements IQuery<T>, IQueryIntern<T> {
    @Autowired
    protected ITransaction transaction;

    @Autowired
    protected IQuery<T> query;

    @Autowired
    protected IQueryIntern<T> queryIntern;

    @Autowired
    protected IQuery<T> transactionalQuery;

    @Override
    public synchronized void dispose() {
        if (query == null) {
            return;
        }
        query.dispose();
        for (Field field : ReflectUtil.getDeclaredFieldsInHierarchy(getClass())) {
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || field.getType().isPrimitive()) {
                continue;
            }
            try {
                field.set(this, null);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw RuntimeExceptionUtil.mask(e);
            }
        }
    }

    @Override
    public Class<T> getEntityType() {
        return query.getEntityType();
    }

    @Override
    public void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes) {
        query.fillRelatedEntityTypes(relatedEntityTypes);
    }

    @Override
    public IQueryKey getQueryKey(Map<Object, Object> nameToValueMap) {
        return query.getQueryKey(nameToValueMap);
    }

    @Override
    public IVersionCursor retrieveAsVersions() {
        return query.retrieveAsVersions();
    }

    @Override
    public IDataCursor retrieveAsData() {
        return query.retrieveAsData();
    }

    @Override
    public IDataCursor retrieveAsData(Map<Object, Object> nameToValueMap) {
        return queryIntern.retrieveAsData(nameToValueMap);
    }

    @Override
    public IVersionCursor retrieveAsVersions(Map<Object, Object> nameToValueMap) {
        return queryIntern.retrieveAsVersions(nameToValueMap, true);
    }

    @Override
    public IVersionCursor retrieveAsVersions(boolean retrieveAlternateIds) {
        return query.retrieveAsVersions(retrieveAlternateIds);
    }

    @Override
    public IList<IObjRef> retrieveAsObjRefs(Map<Object, Object> paramNameToValueMap, int idIndex) {
        return queryIntern.retrieveAsObjRefs(paramNameToValueMap, idIndex);
    }

    @Override
    public IList<IObjRef> retrieveAsObjRefs(int idIndex) {
        return query.retrieveAsObjRefs(idIndex);
    }

    @Override
    public IVersionCursor retrieveAsVersions(Map<Object, Object> paramNameToValueMap, boolean retrieveAlternateIds) {
        return queryIntern.retrieveAsVersions(paramNameToValueMap, retrieveAlternateIds);
    }

    @Override
    public IEntityCursor<T> retrieveAsCursor() {
        return query.retrieveAsCursor();
    }

    @Override
    public IEntityCursor<T> retrieveAsCursor(Map<Object, Object> nameToValueMap) {
        return queryIntern.retrieveAsCursor(nameToValueMap);
    }

    @Override
    public IList<T> retrieve() {
        if (transaction.isActive()) {
            return query.retrieve();
        }
        return transaction.processAndCommitWithResult(persistenceUnitToDatabaseMap -> query.retrieve(), true, true, false);
    }

    @Override
    public IList<T> retrieve(Map<Object, Object> nameToValueMap) {
        if (transaction.isActive()) {
            return queryIntern.retrieve(nameToValueMap);
        }
        return transaction.processAndCommitWithResult(persistenceUnitToDatabaseMap -> queryIntern.retrieve(nameToValueMap), true, true, false);
    }

    @Override
    public T retrieveSingle() {
        return transactionalQuery.retrieveSingle();
    }

    @Override
    public IQuery<T> param(Object paramKey, Object param) {
        return query.param(paramKey, param);
    }

    @Override
    public long count() {
        if (transaction.isActive()) {
            return query.count();
        }
        return transaction.processAndCommitWithResult(persistenceUnitToDatabaseMap -> Long.valueOf(query.count()), true, true, false).longValue();
    }

    @Override
    public long count(Map<Object, Object> paramNameToValueMap) {
        if (transaction.isActive()) {
            return queryIntern.count(paramNameToValueMap);
        }
        return transaction.processAndCommitWithResult(persistenceUnitToDatabaseMap -> Long.valueOf(queryIntern.count(paramNameToValueMap)), true, true, false).intValue();
    }

    @Override
    public boolean isEmpty() {
        if (transaction.isActive()) {
            return query.isEmpty();
        }
        return transaction.processAndCommitWithResult(persistenceUnitToDatabaseMap -> Boolean.valueOf(query.isEmpty()), true, true, false).booleanValue();
    }

    @Override
    public boolean isEmpty(Map<Object, Object> paramNameToValueMap) {
        if (transaction.isActive()) {
            return queryIntern.isEmpty(paramNameToValueMap);
        }
        return transaction.processAndCommitWithResult(persistenceUnitToDatabaseMap -> Boolean.valueOf(queryIntern.isEmpty(paramNameToValueMap)), true, true, false).booleanValue();
    }
}
