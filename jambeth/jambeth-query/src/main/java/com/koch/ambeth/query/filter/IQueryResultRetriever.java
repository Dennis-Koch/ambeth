package com.koch.ambeth.query.filter;

import java.util.List;

public interface IQueryResultRetriever
{
	IQueryResultCacheItem getQueryResult();

	List<Class<?>> getRelatedEntityTypes();

	boolean containsPageOnly();
}
