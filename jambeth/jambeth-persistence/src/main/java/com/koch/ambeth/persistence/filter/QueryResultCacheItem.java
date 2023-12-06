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

import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.query.filter.IQueryResultCacheItem;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.Arrays;

public class QueryResultCacheItem implements IQueryResultCacheItem {
    protected final Class<?> entityType;

    protected final long totalSize;

    protected final int size;

    protected final Object[] idArrays;

    protected final Arrays.PreparedArrayGet[] idArrayGetters;

    protected final Object versionArray;

    protected final Arrays.PreparedArrayGet versionArrayGetter;

    public QueryResultCacheItem(Class<?> entityType, long totalSize, int size, Object[] idArrays, Object versionArray) {
        super();
        this.entityType = entityType;
        this.totalSize = totalSize;
        this.size = size;
        this.idArrays = idArrays;
        this.versionArray = versionArray;
        idArrayGetters = java.util.Arrays.stream(idArrays).map(Arrays::prepareGet).toArray(Arrays.PreparedArrayGet[]::new);
        versionArrayGetter = versionArray != null ? Arrays.prepareGet(versionArray) : null;
    }

    @Override
    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public IObjRef getObjRef(int index, byte idIndex) {
        var id = idArrayGetters[idIndex + 1].get(index);
        var version = versionArrayGetter != null ? versionArrayGetter.get(index) : null;
        return new ObjRef(entityType, idIndex, id, version);
    }
}
