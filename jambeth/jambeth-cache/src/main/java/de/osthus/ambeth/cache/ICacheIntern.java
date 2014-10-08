package de.osthus.ambeth.cache;

import java.util.List;
import java.util.Set;

import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;

public interface ICacheIntern extends ICache
{
	Object createCacheValueInstance(IEntityMetaData metaData, Object obj);

	void addDirect(IEntityMetaData metaData, Object id, Object version, Object primitiveFilledObject, Object parentCacheValueOrArray, IObjRef[][] relations);

	Object getObject(IObjRef oriToGet, ICacheIntern targetCache, Set<CacheDirective> cacheDirective);

	IList<Object> getObjects(List<IObjRef> orisToGet, ICacheIntern targetCache, Set<CacheDirective> cacheDirective);

	IList<IObjRelationResult> getObjRelations(List<IObjRelation> objRelation, ICacheIntern targetCache, Set<CacheDirective> cacheDirective);

	boolean acquireHardRefTLIfNotAlready();

	void clearHardRefs(boolean acquirementSuccessful);
}
