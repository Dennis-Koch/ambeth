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

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.RootCache;
import com.koch.ambeth.util.collections.IdentityHashMap;

public class CacheDependencyNodeFactory {
    public static CacheDependencyNode addRootCache(IRootCache rootCache, IdentityHashMap<IRootCache, CacheDependencyNode> rootCacheToNodeMap) {
        rootCache = rootCache.getCurrentRootCache();
        var node = rootCacheToNodeMap.get(rootCache);
        if (node != null) {
            return node;
        }
        CacheDependencyNode parentNode = null;
        var parent = ((RootCache) rootCache).getParent();
        if (parent != null) {
            parentNode = addRootCache(parent, rootCacheToNodeMap);
        }
        node = new CacheDependencyNode(rootCache);
        node.parentNode = parentNode;
        if (parentNode != null) {
            parentNode.childNodes.add(node);
        }
        rootCacheToNodeMap.put(rootCache, node);
        return node;
    }

    public static CacheDependencyNode buildRootNode(IdentityHashMap<IRootCache, CacheDependencyNode> secondLevelCacheToNodeMap) {
        CacheDependencyNode rootNode = null;
        for (var entry : secondLevelCacheToNodeMap) {
            var node = entry.getValue();
            if (node.parentNode == null) {
                if (rootNode != null) {
                    throw new IllegalStateException("Must never happen");
                }
                rootNode = node;
            }
        }
        return rootNode;
    }
}
