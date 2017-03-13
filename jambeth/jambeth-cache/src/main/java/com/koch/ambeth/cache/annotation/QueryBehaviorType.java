package com.koch.ambeth.cache.annotation;

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
