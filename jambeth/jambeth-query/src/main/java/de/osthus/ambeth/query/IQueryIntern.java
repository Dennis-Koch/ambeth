package de.osthus.ambeth.query;

import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IEntityCursor;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.util.IDisposable;

public interface IQueryIntern<T> extends IDisposable
{
	IVersionCursor retrieveAsVersions();

	IDataCursor retrieveAsData(IMap<Object, Object> paramNameToValueMap);

	IVersionCursor retrieveAsVersions(IMap<Object, Object> paramNameToValueMap);

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
