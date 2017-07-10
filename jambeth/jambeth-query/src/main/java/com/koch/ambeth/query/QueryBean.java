package com.koch.ambeth.query;

/*-
 * #%L
 * jambeth-query
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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.List;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IEntityCursor;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class QueryBean<T> implements IQuery<T> {
	@Autowired
	protected Class<T> entityType;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	@Autowired
	protected IQueryCreator queryCreator;

	protected Reference<IQuery<T>> queryRef;

	@Override
	public void dispose() {
		queryRef = null;
	}

	protected IQuery<T> getQuery() {
		IQuery<T> query = null;
		if (queryRef != null) {
			query = queryRef.get();
		}
		if (query == null) {
			IQueryBuilder<T> queryBuilder = queryBuilderFactory.create(entityType);
			query = queryCreator.createCustomQuery(queryBuilder);
			if (query == null) {
				throw new IllegalStateException(
						"QueryCreator " + queryCreator + " returned no query handle");
			}
			queryRef = new SoftReference<>(query);
		}
		return query;
	}

	@Override
	public Class<T> getEntityType() {
		return entityType;
	}

	@Override
	public void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public IQueryKey getQueryKey(IMap<Object, Object> nameToValueMap) {
		return getQuery().getQueryKey(nameToValueMap);
	}

	@Override
	@Deprecated
	public IVersionCursor retrieveAsVersions(IMap<Object, Object> nameToValueMap) {
		return getQuery().retrieveAsVersions(nameToValueMap);
	}

	@Override
	@Deprecated
	public IEntityCursor<T> retrieveAsCursor(IMap<Object, Object> nameToValueMap) {
		return getQuery().retrieveAsCursor(nameToValueMap);
	}

	@Override
	@Deprecated
	public IList<T> retrieve(IMap<Object, Object> nameToValueMap) {
		return getQuery().retrieve(nameToValueMap);
	}

	@Override
	public IList<T> retrieve() {
		return getQuery().retrieve();
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor() {
		return getQuery().retrieveAsCursor();
	}

	@Override
	public IVersionCursor retrieveAsVersions() {
		return getQuery().retrieveAsVersions();
	}

	@Override
	public IVersionCursor retrieveAsVersions(boolean retrieveAlternateIds) {
		return getQuery().retrieveAsVersions(retrieveAlternateIds);
	}

	@Override
	public IDataCursor retrieveAsData() {
		return getQuery().retrieveAsData();
	}

	@Override
	public IList<IObjRef> retrieveAsObjRefs(int idIndex) {
		return getQuery().retrieveAsObjRefs(idIndex);
	}

	@Override
	public T retrieveSingle() {
		return getQuery().retrieveSingle();
	}

	@Override
	public long count() {
		return getQuery().count();
	}

	@Override
	public boolean isEmpty() {
		return getQuery().isEmpty();
	}

	@Override
	public IQuery<T> param(Object paramKey, Object param) {
		return getQuery().param(paramKey, param);
	}
}
