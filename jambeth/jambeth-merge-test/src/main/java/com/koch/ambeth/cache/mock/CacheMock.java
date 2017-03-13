package com.koch.ambeth.cache.mock;

import java.util.List;
import java.util.Set;

import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.HandleContentDelegate;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.Lock;
import com.koch.ambeth.util.collections.IList;

/**
 * Support for unit tests that do not include jAmbeth.Cache
 */
public class CacheMock implements ICache {
	@Override
	public ICache getCurrentCache() {
		return this;
	}

	@Override
	public boolean isPrivileged() {
		return true;
	}

	@Override
	public <E> E getObject(Class<E> type, Object... compositeIdParts) {
		return null;
	}

	@Override
	public <E> IList<E> getObjects(Class<E> type, Object... ids) {
		return null;
	}

	@Override
	public <E> IList<E> getObjects(Class<E> type, List<?> ids) {
		return null;
	}

	@Override
	public IList<Object> getObjects(IObjRef[] orisToGetArray, Set<CacheDirective> cacheDirective) {
		return null;
	}

	@Override
	public IList<Object> getObjects(List<IObjRef> orisToGet, Set<CacheDirective> cacheDirective) {
		return null;
	}

	@Override
	public IList<IObjRelationResult> getObjRelations(List<IObjRelation> self,
			Set<CacheDirective> cacheDirective) {
		return null;
	}

	@Override
	public Object getObject(IObjRef oriToGet, Set<CacheDirective> cacheDirective) {
		return null;
	}

	@Override
	public <E> E getObject(Class<E> type, Object id) {
		return null;
	}

	@Override
	public <E> E getObject(Class<E> type, String idName, Object id) {
		return null;
	}

	@Override
	public <E> E getObject(Class<E> type, Object id, Set<CacheDirective> cacheDirective) {
		return null;
	}

	@Override
	public <E> E getObject(Class<E> type, String idName, Object id,
			Set<CacheDirective> cacheDirective) {
		return null;
	}

	@Override
	public void getContent(HandleContentDelegate handleContentDelegate) {
	}

	@Override
	public Lock getReadLock() {
		return null;
	}

	@Override
	public Lock getWriteLock() {
		return null;
	}

	@Override
	public void cascadeLoadPath(Class<?> entityType, String cascadeLoadPath) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
