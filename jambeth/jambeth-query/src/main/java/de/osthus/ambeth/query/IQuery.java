package de.osthus.ambeth.query;

import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IEntityCursor;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.util.IDisposable;

public interface IQuery<T> extends IDisposable
{
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
