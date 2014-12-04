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

	IDataCursor retrieveAsData(IMap<Object, Object> nameToValueMap);

	IVersionCursor retrieveAsVersions(IMap<Object, Object> nameToValueMap);

	IEntityCursor<T> retrieveAsCursor();

	IEntityCursor<T> retrieveAsCursor(IMap<Object, Object> nameToValueMap);

	IList<T> retrieve();

	IList<T> retrieve(IMap<Object, Object> nameToValueMap);

	Class<T> getEntityType();

	void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes);

	IQueryKey getQueryKey(IMap<Object, Object> nameToValueMap);
}
