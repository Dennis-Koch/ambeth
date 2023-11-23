package com.koch.ambeth.query;

/*-
 * #%L
 * jambeth-query
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

import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IEntityCursor;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

import java.util.List;

public class StatefulQuery<T> implements IQuery<T> {
    protected final HashMap<Object, Object> paramMap = new HashMap<>();
    protected IQueryIntern<T> query;

    public StatefulQuery(IQueryIntern<T> query) {
        this.query = query;
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
    public IQueryKey getQueryKey(IMap<Object, Object> nameToValueMap) {
        return query.getQueryKey(nameToValueMap);
    }

    @Override
    public void dispose() {
        query = null;
        paramMap.clear();
    }

    @Override
    public IVersionCursor retrieveAsVersions() {
        return query.retrieveAsVersions(paramMap, true);
    }

    @Override
    public IVersionCursor retrieveAsVersions(boolean retrieveAlternateIds) {
        return query.retrieveAsVersions(paramMap, retrieveAlternateIds);
    }

    @Override
    public IDataCursor retrieveAsData() {
        return query.retrieveAsData(paramMap);
    }

    @Override
    public IVersionCursor retrieveAsVersions(IMap<Object, Object> nameToValueMap) {
        throw new UnsupportedOperationException("Only retrieveAsVersions() allowed");
    }

    @Override
    public IList<IObjRef> retrieveAsObjRefs(int idIndex) {
        return query.retrieveAsObjRefs(paramMap, idIndex);
    }

    @Override
    public IEntityCursor<T> retrieveAsCursor() {
        return query.retrieveAsCursor(paramMap);
    }

    @Override
    public IEntityCursor<T> retrieveAsCursor(IMap<Object, Object> nameToValueMap) {
        throw new UnsupportedOperationException("Only retrieveAsCursor() allowed");
    }

    @Override
    public IList<T> retrieve() {
        return query.retrieve(paramMap);
    }

    @Override
    public IList<T> retrieve(IMap<Object, Object> nameToValueMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IQuery<T> param(Object paramKey, Object param) {
        if (!paramMap.putIfNotExists(paramKey, param)) {
            throw new IllegalArgumentException("Parameter '" + paramKey + "' already added with value '" + paramMap.get(paramKey) + "'");
        }
        return this;
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
        return query.count(paramMap);
    }

    @Override
    public boolean isEmpty() {
        return query.isEmpty(paramMap);
    }
}
