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
import com.koch.ambeth.filter.PagingRequest;
import com.koch.ambeth.filter.PagingResponse;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.query.IQueryIntern;
import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.query.config.QueryConfigurationConstants;
import com.koch.ambeth.query.filter.IPagingQuery;
import com.koch.ambeth.query.filter.IQueryResultCache;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PagingQuery<T> implements IPagingQuery<T>, IPagingQueryIntern<T> {
    @Autowired
    protected IServiceContext beanContext;
    @Autowired
    protected ICache cache;
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;
    @Autowired
    protected IThreadLocalObjectCollector objectCollector;
    @Autowired
    protected IQueryIntern<Object> query;
    @Autowired
    protected IQueryResultCache queryResultCache;
    @Property(name = QueryConfigurationConstants.PagingPrefetchBehavior, defaultValue = "false")
    protected boolean prefetchAllPages;
    @LogInstance
    private ILogger log;

    @Override
    public void dispose() {
        beanContext = null;
        cache = null;
        entityMetaDataProvider = null;
        objectCollector = null;
        queryResultCache = null;
        if (query != null) {
            query.dispose();
            query = null;
        }
    }

    @Override
    public IPagingRequest createRequest(int pageNumber, int sizePerPage) {
        var request = new PagingRequest();
        request.setNumber(pageNumber);
        request.setSize(sizePerPage);
        return request;
    }

    @Override
    public IQueryKey getQueryKey(Map<Object, Object> nameToValueMap) {
        return query.getQueryKey(nameToValueMap);
    }

    @Override
    public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest) {
        var metaData = entityMetaDataProvider.getMetaData(query.getEntityType());
        return retrieveIntern(pagingRequest, metaData.getIdMember().getName(), null);
    }

    @Override
    public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, String alternateIdPropertyName) {
        return retrieveIntern(pagingRequest, alternateIdPropertyName, null);
    }

    @Override
    public IPagingResponse<T> retrieve(IPagingRequest pagingRequest) {
        return retrieveIntern(pagingRequest, null, null);
    }

    @Override
    public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, Map<Object, Object> paramMap) {
        var metaData = entityMetaDataProvider.getMetaData(query.getEntityType());
        return retrieveIntern(pagingRequest, metaData.getIdMember().getName(), paramMap);
    }

    @Override
    public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, String alternateIdPropertyName, Map<Object, Object> paramMap) {
        return retrieveIntern(pagingRequest, alternateIdPropertyName, paramMap);
    }

    @Override
    public IPagingResponse<T> retrieve(IPagingRequest pagingRequest, Map<Object, Object> paramMap) {
        return retrieveIntern(pagingRequest, null, paramMap);
    }

    protected IPagingResponse<T> retrieveIntern(IPagingRequest pagingRequest, String alternateIdPropertyName, Map<Object, Object> nameToValueMap) {
        long start = System.currentTimeMillis();
        var tlObjectCollector = objectCollector.getCurrent();
        var currentNameToValueMap = new HashMap<>();
        if (nameToValueMap != null) {
            currentNameToValueMap.putAll(nameToValueMap);
        }
        var pagingResponse = new PagingResponse<T>();

        int offset, length;

        if (pagingRequest != null) {
            var number = pagingRequest.getNumber();
            length = pagingRequest.getSize();

            if (number < 1 || length < 1) {
                offset = -1;
                number = 0;
                length = 0;
            } else {
                offset = (number - 1) * length;
                if (!prefetchAllPages) {
                    currentNameToValueMap.put(QueryConstants.PAGING_INDEX_OBJECT, offset);
                    currentNameToValueMap.put(QueryConstants.PAGING_SIZE_OBJECT, length);
                }
            }

            pagingResponse.setNumber(number);
        } else {
            offset = 0;
            length = -1;
        }
        pagingResponse.setTotalNumber(-1);
        pagingResponse.setTotalSize(-1);

        var totalSize = new ParamHolder<Long>();

        var metaData = entityMetaDataProvider.getMetaData(query.getEntityType());
        var idIndex = alternateIdPropertyName == null ? ObjRef.PRIMARY_KEY_INDEX : metaData.getIdIndexByMemberName(alternateIdPropertyName);

        var queryResultRetrieverBC = beanContext.registerBean(DefaultQueryResultRetriever.class);
        queryResultRetrieverBC.propertyValue("Query", query);
        queryResultRetrieverBC.propertyValue("CurrentNameToValueMap", currentNameToValueMap);
        queryResultRetrieverBC.propertyValue("Size", length);
        var queryResultRetriever = queryResultRetrieverBC.finish();

        var queryKey = query.getQueryKey(currentNameToValueMap);
        var queryRefResult = queryResultCache.getQueryResult(queryKey, queryResultRetriever, idIndex, offset, length, totalSize);

        pagingResponse.setTotalSize(totalSize.getValue().longValue());
        if (length <= 0) {
            // No Paging or zero-length paging (the latter is a rare usecase) means there is 1 page
            // with all data
            pagingResponse.setTotalNumber(1);
        } else {
            // Calculate page count by the length of each page in relation to the overall length of the
            // result
            pagingResponse.setTotalNumber((int) ((pagingResponse.getTotalSize() + length - 1) / length));
        }
        if (alternateIdPropertyName != null) {
            pagingResponse.setRefResult(queryRefResult);
        } else {
            @SuppressWarnings("unchecked") List<T> result = (List<T>) cache.getObjects(queryRefResult, Collections.<CacheDirective>emptySet());
            pagingResponse.setResult(result);
        }
        var end = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            if (alternateIdPropertyName != null) {
                log.debug(StringBuilderUtil.concat(tlObjectCollector, "Spent ", end - start, " ms executing query returning ", pagingResponse.getRefResult().size(), " ORIs of entity type '",
                        query.getEntityType(), "'"));
            } else {
                log.debug(StringBuilderUtil.concat(tlObjectCollector, "Spent ", end - start, " ms executing query returning ", pagingResponse.getResult().size(), " instances of entity type '",
                        query.getEntityType(), "'"));
            }
        }
        return pagingResponse;
    }

    @Override
    public IPagingQuery<T> param(Object paramKey, Object param) {
        var parameterizedQuery = new ParameterizedPagingQuery<>(this);
        return parameterizedQuery.param(paramKey, param);
    }
}
