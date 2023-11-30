package com.koch.ambeth.persistence.filter;

/*-
 * #%L
 * jambeth-persistence
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

import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.query.filter.IPagingQuery;
import com.koch.ambeth.util.collections.HashMap;

import java.util.Map;

public class StatefulPagingQuery<T> implements IPagingQuery<T> {
    protected final HashMap<Object, Object> paramMap = new HashMap<>();
    protected IPagingQueryIntern<T> pagingQuery;

    public StatefulPagingQuery(IPagingQueryIntern<T> pagingQuery) {
        this.pagingQuery = pagingQuery;
    }

    @Override
    public void dispose() {
        pagingQuery = null;
        paramMap.clear();
    }

    @Override
    public IPagingRequest createRequest(int pageNumber, int sizePerPage) {
        return pagingQuery.createRequest(pageNumber, sizePerPage);
    }

    @Override
    public IQueryKey getQueryKey(Map<Object, Object> nameToValueMap) {
        return pagingQuery.getQueryKey(nameToValueMap);
    }

    @Override
    public IPagingQuery<T> param(Object paramKey, Object param) {
        if (!paramMap.putIfNotExists(paramKey, param)) {
            throw new IllegalArgumentException("Parameter '" + paramKey + "' already added with value '" + paramMap.get(paramKey) + "'");
        }
        return this;
    }

    @Override
    public IPagingResponse<T> retrieve(IPagingRequest pagingRequest) {
        return pagingQuery.retrieve(pagingRequest, paramMap);
    }

    @Override
    public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest) {
        return pagingQuery.retrieveRefs(pagingRequest, paramMap);
    }

    @Override
    public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, String alternateIdPropertyName) {
        return pagingQuery.retrieveRefs(pagingRequest, alternateIdPropertyName, paramMap);
    }
}
