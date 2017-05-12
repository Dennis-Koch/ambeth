using De.Osthus.Ambeth.Collections;
using System;

namespace De.Osthus.Ambeth.Cache
{
    public class CacheDependencyNodeFactory
    {
        public static CacheDependencyNode AddRootCache(IRootCache rootCache, IdentityHashMap<IRootCache, CacheDependencyNode> rootCacheToNodeMap)
        {
            rootCache = rootCache.CurrentRootCache;
            CacheDependencyNode node = rootCacheToNodeMap.Get(rootCache);
            if (node != null)
            {
                return node;
            }
            CacheDependencyNode parentNode = null;
            IRootCache parent = ((RootCache)rootCache).Parent;
            if (parent != null)
            {
                parentNode = AddRootCache(parent, rootCacheToNodeMap);
            }
            node = new CacheDependencyNode(rootCache);
            node.parentNode = parentNode;
            if (parentNode != null)
            {
                parentNode.childNodes.Add(node);
            }
            rootCacheToNodeMap.Put(rootCache, node);
            return node;
        }

        public static CacheDependencyNode BuildRootNode(IdentityHashMap<IRootCache, CacheDependencyNode> secondLevelCacheToNodeMap)
	    {
		    CacheDependencyNode rootNode = null;
		    foreach (Entry<IRootCache, CacheDependencyNode> entry in secondLevelCacheToNodeMap)
		    {
			    CacheDependencyNode node = entry.Value;
			    if (node.parentNode == null)
			    {
				    if (rootNode != null)
				    {
					    throw new Exception("Must never happen");
				    }
				    rootNode = node;
			    }
		    }
		    return rootNode;
	    }
    }
}