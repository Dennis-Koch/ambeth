package com.koch.ambeth.cache.datachange;

import java.util.Map.Entry;

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.RootCache;
import com.koch.ambeth.util.collections.IdentityHashMap;

public class CacheDependencyNodeFactory
{
	public static CacheDependencyNode addRootCache(IRootCache rootCache, IdentityHashMap<IRootCache, CacheDependencyNode> rootCacheToNodeMap)
	{
		rootCache = rootCache.getCurrentRootCache();
		CacheDependencyNode node = rootCacheToNodeMap.get(rootCache);
		if (node != null)
		{
			return node;
		}
		CacheDependencyNode parentNode = null;
		IRootCache parent = ((RootCache) rootCache).getParent();
		if (parent != null)
		{
			parentNode = addRootCache(parent, rootCacheToNodeMap);
		}
		node = new CacheDependencyNode(rootCache);
		node.parentNode = parentNode;
		if (parentNode != null)
		{
			parentNode.childNodes.add(node);
		}
		rootCacheToNodeMap.put(rootCache, node);
		return node;
	}

	public static CacheDependencyNode buildRootNode(IdentityHashMap<IRootCache, CacheDependencyNode> secondLevelCacheToNodeMap)
	{
		CacheDependencyNode rootNode = null;
		for (Entry<IRootCache, CacheDependencyNode> entry : secondLevelCacheToNodeMap)
		{
			CacheDependencyNode node = entry.getValue();
			if (node.parentNode == null)
			{
				if (rootNode != null)
				{
					throw new IllegalStateException("Must never happen");
				}
				rootNode = node;
			}
		}
		return rootNode;
	}
}
