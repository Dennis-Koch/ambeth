package com.koch.ambeth.cache.annotation;

/*-
 * #%L
 * jambeth-cache
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

public enum QueryBehaviorType {
	/**
	 * All queries built by <code>com.koch.ambeth.query.IQueryBuilder</code> will behave 'normally'.
	 *
	 * That explicitly means that a call to <code>com.koch.ambeth.query.IQuery.retrieve()</code> will
	 * return a list of retrieved entities. These entities can be processed, filtered, transformed by
	 * any means before returning the service result
	 */
	DEFAULT,

	/**
	 * All queries built by <code>com.koch.ambeth.query.IQueryBuilder</code> will run in 'high
	 * performance mode'.
	 *
	 *
	 * This option is only active if a root-caller on the current thread stack is a
	 * <code>com.koch.ambeth.cache.service.ICacheService</code>-instance. Will do nothing (runs in
	 * <code>com.koch.ambeth.cache.annotation.QueryBehaviorType.DEFAULT</code>-mode) if the service is
	 * run by any other means.
	 *
	 * IF the root-caller is a <code>com.koch.ambeth.cache.service.ICacheService</code>-instance then
	 * a call to enclosed <code>com.koch.ambeth.query.IQuery</code> -instances will SEEM to do
	 * nothing. They will not return entities. Instead a list of <code>IObjRef</code> instances is
	 * retrieved and stored internally. This list will be used by the initially calling
	 * <code>com.koch.ambeth.cache.service.ICacheService</code>-instance to return the cache-request
	 * without transferring unnecessary payload to the requestor.
	 */
	OBJREF_ONLY
}
