package com.koch.ambeth.query;

import java.util.List;

import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IEntityCursor;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public interface IQueryIntern<T> extends IDisposable
{
	IVersionCursor retrieveAsVersions();

	IDataCursor retrieveAsData(IMap<Object, Object> paramNameToValueMap);

	IVersionCursor retrieveAsVersions(IMap<Object, Object> paramNameToValueMap, boolean retrieveAlternateIds);

	IEntityCursor<T> retrieveAsCursor();

	IEntityCursor<T> retrieveAsCursor(IMap<Object, Object> paramNameToValueMap);

	IList<T> retrieve();

	IList<T> retrieve(IMap<Object, Object> paramNameToValueMap);

	Class<T> getEntityType();

	void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes);

	IQueryKey getQueryKey(IMap<Object, Object> paramNameToValueMap);

	long count(IMap<Object, Object> paramNameToValueMap);

	boolean isEmpty(IMap<Object, Object> paramNameToValueMap);
}
