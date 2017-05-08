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

import java.util.List;

import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IEntityCursor;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public interface IQuery<T> extends IDisposable {
	Class<T> getEntityType();

	void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes);

	IQueryKey getQueryKey(IMap<Object, Object> nameToValueMap);

	IVersionCursor retrieveAsVersions();

	IVersionCursor retrieveAsVersions(boolean retrieveAlternateIds);

	IDataCursor retrieveAsData();

	long count();

	boolean isEmpty();

	/**
	 * Please use param(Object, Object) instead
	 *
	 * @param nameToValueMap
	 * @return
	 */
	@Deprecated
	IVersionCursor retrieveAsVersions(IMap<Object, Object> nameToValueMap);

	IEntityCursor<T> retrieveAsCursor();

	/**
	 * Please use param(Object, Object) instead
	 *
	 * @param nameToValueMap
	 * @return
	 */
	@Deprecated
	IEntityCursor<T> retrieveAsCursor(IMap<Object, Object> nameToValueMap);

	IList<T> retrieve();

	T retrieveSingle();

	/**
	 * Please use param(Object, Object) instead
	 *
	 * @param nameToValueMap
	 * @return
	 */
	@Deprecated
	IList<T> retrieve(IMap<Object, Object> nameToValueMap);

	IQuery<T> param(Object paramKey, Object param);
}
