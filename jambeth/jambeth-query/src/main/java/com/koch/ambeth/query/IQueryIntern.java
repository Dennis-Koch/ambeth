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
import com.koch.ambeth.util.IDisposable;

import java.util.List;
import java.util.Map;

public interface IQueryIntern<T> extends IDisposable {
    IVersionCursor retrieveAsVersions();

    IDataCursor retrieveAsData(Map<Object, Object> paramNameToValueMap);

    IVersionCursor retrieveAsVersions(Map<Object, Object> paramNameToValueMap, boolean retrieveAlternateIds);

    List<IObjRef> retrieveAsObjRefs(Map<Object, Object> paramNameToValueMap, int idIndex);

    IEntityCursor<T> retrieveAsCursor(Map<Object, Object> paramNameToValueMap);

    List<T> retrieve(Map<Object, Object> paramNameToValueMap);

    Class<T> getEntityType();
    
    void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes);

    IQueryKey getQueryKey(Map<Object, Object> paramNameToValueMap);

    long count(Map<Object, Object> paramNameToValueMap);

    boolean isEmpty(Map<Object, Object> paramNameToValueMap);
}
