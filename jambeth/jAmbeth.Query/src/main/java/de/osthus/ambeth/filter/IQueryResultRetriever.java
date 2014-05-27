package de.osthus.ambeth.filter;

import java.util.List;

public interface IQueryResultRetriever
{
	IQueryResultCacheItem getQueryResult();

	List<Class<?>> getRelatedEntityTypes();

	boolean containsPageOnly();
}
