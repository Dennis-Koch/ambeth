package com.koch.ambeth.cache;

import java.util.List;
import java.util.Set;

import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IList;

public interface ICacheIntern extends ICache
{
	Object createCacheValueInstance(IEntityMetaData metaData, Object obj);

	void addDirect(IEntityMetaData metaData, Object id, Object version, Object primitiveFilledObject, Object parentCacheValueOrArray, IObjRef[][] relations);

	Object getObject(IObjRef oriToGet, ICacheIntern targetCache, Set<CacheDirective> cacheDirective);

	IList<Object> getObjects(List<IObjRef> orisToGet, ICacheIntern targetCache, Set<CacheDirective> cacheDirective);

	IList<IObjRelationResult> getObjRelations(List<IObjRelation> objRelation, ICacheIntern targetCache, Set<CacheDirective> cacheDirective);

	boolean acquireHardRefTLIfNotAlready();

	void clearHardRefs(boolean acquirementSuccessful);

	void assignEntityToCache(Object entity);
}
