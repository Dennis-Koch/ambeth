package com.koch.ambeth.cache.datachange;

/*-
 * #%L
 * jambeth-cache-datachange
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

import com.koch.ambeth.cache.ChildCache;
import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ISet;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class CacheDependencyNode {
    protected static final Set<CacheDirective> cacheValueResultAndReturnMissesSet = EnumSet.of(CacheDirective.CacheValueResult, CacheDirective.ReturnMisses);

    protected static final Set<CacheDirective> failInCacheHierarchyAndCacheValueResult = EnumSet.of(CacheDirective.FailInCacheHierarchy, CacheDirective.CacheValueResult);

    public final IRootCache rootCache;

    public final HashSet<IObjRef> hardRefObjRefsToLoad = new HashSet<>();

    public final HashSet<IObjRef> objRefsToLoad = new HashSet<>();

    public final ArrayList<ChildCache> directChildCaches = new ArrayList<>();

    public final ArrayList<CacheDependencyNode> childNodes = new ArrayList<>();

    public final HashSet<IObjRef> cascadeRefreshObjRefsSet = new HashSet<>();

    public final HashSet<IObjRelation> cascadeRefreshObjRelationsSet = new HashSet<>();

    public final HashMap<IObjRef, CacheValueAndPrivilege> objRefToCacheValueMap = new HashMap<>();

    public CacheChangeItem[] cacheChangeItems;

    public CacheDependencyNode parentNode;

    @SuppressWarnings("unused")
    private List<Object> privilegedHardRefResult;

    private boolean pendingChangeOnAnyChildCache;

    public CacheDependencyNode(IRootCache rootCache) {
        this.rootCache = rootCache;
    }

    public boolean isPendingChangeOnAnyChildCache() {
        return pendingChangeOnAnyChildCache;
    }

    public void pushPendingChangeOnAnyChildCache(int index, CacheChangeItem cci) {
        if (cacheChangeItems == null) {
            cacheChangeItems = new CacheChangeItem[directChildCaches.size()];
        }
        cacheChangeItems[index] = cci;
        pushPendingChangeOnAnyChildCacheIntern();
    }

    private void pushPendingChangeOnAnyChildCacheIntern() {
        pendingChangeOnAnyChildCache = true;
        if (parentNode != null) {
            parentNode.pushPendingChangeOnAnyChildCacheIntern();
        }
    }

    public void aggregateAllCascadedObjRefs() {
        for (int a = childNodes.size(); a-- > 0; ) {
            CacheDependencyNode childNode = childNodes.get(a);
            childNode.aggregateAllCascadedObjRefs();

            hardRefObjRefsToLoad.addAll(childNode.hardRefObjRefsToLoad);
            objRefsToLoad.addAll(childNode.objRefsToLoad);
        }
    }

    protected void removeNotFoundObjRefs(IObjRef[] objRefsToRemove) {
        hardRefObjRefsToLoad.removeAll(objRefsToRemove);
        objRefsToLoad.removeAll(objRefsToRemove);

        for (int a = childNodes.size(); a-- > 0; ) {
            var childNode = childNodes.get(a);
            childNode.removeNotFoundObjRefs(objRefsToRemove);

            // Hold cache values as hard ref to prohibit cache loss due to GC
            var hardRefRequest = childNode.hardRefObjRefsToLoad.toList();
            childNode.privilegedHardRefResult = childNode.rootCache.getObjects(hardRefRequest, failInCacheHierarchyAndCacheValueResult);
        }
    }

    public ISet<IObjRef> lookForIntermediateDeletes() {
        var intermediateDeletes = new HashSet<IObjRef>();
        // Hold cache values as hard ref to prohibit cache loss due to GC
        var hardRefRequest = hardRefObjRefsToLoad.toList();
        var hardRefResult = rootCache.getObjects(hardRefRequest, cacheValueResultAndReturnMissesSet);
        for (int a = hardRefResult.size(); a-- > 0; ) {
            var hardRef = hardRefResult.get(a);
            if (hardRef != null) {
                continue;
            }
            // Objects are marked as UPDATED in the DCE, but could not be newly retrieved from the server
            // This occurs if a fast DELETE event on the server happened but has not been processed, yet
            var hardRefObjRefToLoad = hardRefRequest.get(a);
            intermediateDeletes.add(hardRefObjRefToLoad);
        }
        privilegedHardRefResult = hardRefResult;
        if (!intermediateDeletes.isEmpty()) {
            var intermediateDeletesArray = intermediateDeletes.toArray(IObjRef[]::new);
            removeNotFoundObjRefs(intermediateDeletesArray);
        }
        return intermediateDeletes;
    }
}
