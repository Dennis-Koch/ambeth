package com.koch.ambeth.query.inmemory.builder;

/*-
 * #%L
 * jambeth-query-inmemory
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

import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryIntern;
import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.query.ParameterizedQuery;
import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IEntityCursor;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.service.merge.model.IObjRef;

import java.util.List;
import java.util.Map;

public class InMemoryQuery<T> implements IQuery<T>, IQueryIntern<T> {
    protected final Class<T> entityType;

    public InMemoryQuery(Class<T> entityType) {
        this.entityType = entityType;

    }

    @Override
    public void dispose() {
        // Intended blank
    }

    @Override
    public Class<T> getEntityType() {
        return entityType;
    }

    @Override
    public void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes) {
    }

    @Override
    public IQueryKey getQueryKey(Map<Object, Object> nameToValueMap) {
        return null;
    }

    @Override
    public IVersionCursor retrieveAsVersions() {
        return null;
    }

    @Override
    public IDataCursor retrieveAsData() {
        return null;
    }

    @Override
    public IVersionCursor retrieveAsVersions(boolean retrieveAlternateIds) {
        return null;
    }

    @Override
    public IVersionCursor retrieveAsVersions(Map<Object, Object> paramNameToValueMap, boolean retrieveAlternateIds) {
        return null;
    }

    @Override
    public IEntityCursor<T> retrieveAsCursor() {
        return null;
    }

    @Override
    public IEntityCursor<T> retrieveAsCursor(Map<Object, Object> nameToValueMap) {
        return null;
    }

    @Override
    public List<T> retrieve() {
        return null;
    }

    @Override
    public T retrieveSingle() {
        return null;
    }

    @Override
    public List<T> retrieve(Map<Object, Object> nameToValueMap) {
        return null;
    }

    @Override
    public IQuery<T> param(Object paramKey, Object param) {
        var parameterizedQuery = new ParameterizedQuery<>(this);
        return parameterizedQuery.param(paramKey, param);
    }

    @Override
    public IDataCursor retrieveAsData(Map<Object, Object> nameToValueMap) {
        return null;
    }

    @Override
    public List<IObjRef> retrieveAsObjRefs(int idIndex) {
        return null;
    }

    @Override
    public List<IObjRef> retrieveAsObjRefs(Map<Object, Object> paramNameToValueMap, int idIndex) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public long count(Map<Object, Object> paramNameToValueMap) {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean isEmpty(Map<Object, Object> paramNameToValueMap) {
        return true;
    }
}
