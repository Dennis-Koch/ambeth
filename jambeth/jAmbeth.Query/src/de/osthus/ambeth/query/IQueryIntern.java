package de.osthus.ambeth.query;

import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IEntityCursor;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.util.IDisposable;

public interface IQueryIntern<T> extends IDisposable
{
	IVersionCursor retrieveAsVersions();

	IDataCursor retrieveAsData(Map<Object, Object> nameToValueMap);

	IVersionCursor retrieveAsVersions(Map<Object, Object> nameToValueMap);

	IEntityCursor<T> retrieveAsCursor();

	IEntityCursor<T> retrieveAsCursor(Map<Object, Object> nameToValueMap);

	IList<T> retrieve();

	IList<T> retrieve(Map<Object, Object> nameToValueMap);

	Class<T> getEntityType();

	void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes);

	IQueryKey getQueryKey(Map<Object, Object> nameToValueMap);
}
