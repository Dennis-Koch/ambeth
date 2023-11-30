package com.koch.ambeth.query.filter;

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

import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.util.IDisposable;

import java.util.Map;

public interface IPagingQuery<T> extends IDisposable {
    IPagingRequest createRequest(int pageNumber, int sizePerPage);

    IQueryKey getQueryKey(Map<Object, Object> nameToValueMap);

    IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest);

    IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, String alternateIdPropertyName);

    IPagingResponse<T> retrieve(IPagingRequest pagingRequest);

    IPagingQuery<T> param(Object paramKey, Object param);
}
