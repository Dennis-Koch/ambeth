package com.koch.ambeth.cache.mock;

/*-
 * #%L
 * jambeth-merge-test
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
