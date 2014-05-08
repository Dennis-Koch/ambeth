package de.osthus.ambeth.query;

import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IEntityCursor;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.util.IDisposable;

public interface IQuery<T> extends IDisposable
{
	Class<T> getEntityType();

	void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes);

	IQueryKey getQueryKey(Map<Object, Object> nameToValueMap);

	IVersionItem retrieveAsVersion();

	IVersionCursor retrieveAsVersions();

	IDataCursor retrieveAsData();

	/**
	 * Please use param(Object, Object) instead
	 * 
	 * @param nameToValueMap
	 * @return
	 */
	@Deprecated
	IVersionCursor retrieveAsVersions(Map<Object, Object> nameToValueMap);

	IEntityCursor<T> retrieveAsCursor();

	/**
	 * Please use param(Object, Object) instead
	 * 
	 * @param nameToValueMap
	 * @return
	 */
	@Deprecated
	IEntityCursor<T> retrieveAsCursor(Map<Object, Object> nameToValueMap);

	IList<T> retrieve();

	T retrieveSingle();

	/**
	 * Please use param(Object, Object) instead
	 * 
	 * @param nameToValueMap
	 * @return
	 */
	@Deprecated
	IList<T> retrieve(Map<Object, Object> nameToValueMap);

	IQuery<T> param(Object paramKey, Object param);
}
