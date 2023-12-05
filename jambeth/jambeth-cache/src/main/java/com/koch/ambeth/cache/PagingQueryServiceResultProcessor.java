package com.koch.ambeth.cache;

/*-
 * #%L
 * jambeth-cache
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

import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.service.cache.IServiceResultProcessor;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.annotation.Find;
import com.koch.ambeth.util.annotation.QueryResultType;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import java.lang.annotation.Annotation;
import java.util.List;

public class PagingQueryServiceResultProcessor implements IServiceResultProcessor {
    @Override
    public Object processServiceResult(Object result, List<IObjRef> objRefs, List<Object> entities, Class<?> expectedType, Object[] serviceRequestArgs, Annotation annotation) {
        @SuppressWarnings("unchecked") var pagingResponse = (IPagingResponse<Object>) result;

        var queryResultType = QueryResultType.REFERENCES;
        if (annotation instanceof Find) {
            queryResultType = ((Find) annotation).resultType();
        }
        switch (queryResultType) {
            case BOTH:
                pagingResponse.setRefResult(objRefs);
                pagingResponse.setResult(entities);
                break;
            case ENTITIES:
                pagingResponse.setRefResult(null);
                pagingResponse.setResult(entities);
                break;
            case REFERENCES:
                pagingResponse.setRefResult(objRefs);
                pagingResponse.setResult(null);
                break;
            default:
                throw RuntimeExceptionUtil.createEnumNotSupportedException(queryResultType);
        }
        return pagingResponse;
    }
}
