using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Cache.Rootcachevalue;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Merge.Model;
using System;
using System.Collections.Generic;
namespace De.Osthus.Ambeth.Cache
{
    public class CacheDependencyNode
    {
	    protected static readonly CacheDirective cacheValueResultAndReturnMissesSet = CacheDirective.CacheValueResult | CacheDirective.ReturnMisses;

	    protected static readonly CacheDirective failInCacheHierarchyAndCacheValueResult = CacheDirective.FailInCacheHierarchy | CacheDirective.CacheValueResult;

	    public readonly IRootCache rootCache;

        public readonly CHashSet<IObjRef> hardRefObjRefsToLoad = new CHashSet<IObjRef>();

        public readonly CHashSet<IObjRef> objRefsToLoad = new CHashSet<IObjRef>();

	    public readonly List<ChildCache> directChildCaches = new List<ChildCache>();

	    public readonly List<CacheDependencyNode> childNodes = new List<CacheDependencyNode>();

        public readonly CHashSet<IObjRef> cascadeRefreshObjRefsSet = new CHashSet<IObjRef>();

        public readonly CHashSet<IObjRelation> cascadeRefreshObjRelationsSet = new CHashSet<IObjRelation>();

        public readonly HashMap<IObjRef, CacheValueAndPrivilege> objRefToCacheValueMap = new HashMap<IObjRef, CacheValueAndPrivilege>();

	    public CacheChangeItem[] cacheChangeItems;

	    public CacheDependencyNode parentNode;

	    private IList<Object> privilegedHardRefResult;

	    private bool pendingChangeOnAnyChildCache;

	    public CacheDependencyNode(IRootCache rootCache)
	    {
		    this.rootCache = rootCache;
	    }

	    public bool IsPendingChangeOnAnyChildCache()
	    {
		    return pendingChangeOnAnyChildCache;
	    }

	    public void PushPendingChangeOnAnyChildCache(int index, CacheChangeItem cci)
	    {
		    if (cacheChangeItems == null)
		    {
			    cacheChangeItems = new CacheChangeItem[directChildCaches.Count];
		    }
		    cacheChangeItems[index] = cci;
		    PushPendingChangeOnAnyChildCacheIntern();
	    }

	    private void PushPendingChangeOnAnyChildCacheIntern()
	    {
		    this.pendingChangeOnAnyChildCache = true;
		    if (parentNode != null)
		    {
			    parentNode.PushPendingChangeOnAnyChildCacheIntern();
		    }
	    }

	    public void AggregateAllCascadedObjRefs()
	    {
		    for (int a = childNodes.Count; a-- > 0;)
		    {
			    CacheDependencyNode childNode = childNodes[a];
			    childNode.AggregateAllCascadedObjRefs();

			    hardRefObjRefsToLoad.AddAll(childNode.hardRefObjRefsToLoad);
			    objRefsToLoad.AddAll(childNode.objRefsToLoad);
		    }
	    }

	    protected void RemoveNotFoundObjRefs(IObjRef[] objRefsToRemove)
	    {
		    hardRefObjRefsToLoad.RemoveAll(objRefsToRemove);
		    objRefsToLoad.RemoveAll(objRefsToRemove);

		    for (int a = childNodes.Count; a-- > 0;)
		    {
			    CacheDependencyNode childNode = childNodes[a];
			    childNode.RemoveNotFoundObjRefs(objRefsToRemove);

			    // Hold cache values as hard ref to prohibit cache loss due to GC
			    IList<IObjRef> hardRefRequest = childNode.hardRefObjRefsToLoad.ToList();
			    childNode.privilegedHardRefResult = childNode.rootCache.GetObjects(hardRefRequest, failInCacheHierarchyAndCacheValueResult);
		    }
	    }

	    public ISet<IObjRef> lookForIntermediateDeletes()
	    {
            CHashSet<IObjRef> intermediateDeletes = new CHashSet<IObjRef>();
		    // Hold cache values as hard ref to prohibit cache loss due to GC
		    IList<IObjRef> hardRefRequest = hardRefObjRefsToLoad.ToList();
		    IList<Object> hardRefResult = rootCache.GetObjects(hardRefRequest, cacheValueResultAndReturnMissesSet);
		    for (int a = hardRefResult.Count; a-- > 0;)
		    {
			    Object hardRef = hardRefResult[a];
			    if (hardRef != null)
			    {
				    continue;
			    }
			    // Objects are marked as UPDATED in the DCE, but could not be newly retrieved from the server
			    // This occurs if a fast DELETE event on the server happened but has not been processed, yet
			    IObjRef hardRefObjRefToLoad = hardRefRequest[a];
			    intermediateDeletes.Add(hardRefObjRefToLoad);
		    }
		    this.privilegedHardRefResult = hardRefResult;
		    RemoveNotFoundObjRefs(intermediateDeletes.ToArray());

		    return intermediateDeletes;
	    }
    }
}