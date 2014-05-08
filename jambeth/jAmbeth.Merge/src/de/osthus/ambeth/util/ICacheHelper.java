package de.osthus.ambeth.util;

import java.util.List;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;

public interface ICacheHelper
{
	/**
	 * Please use <code>de.osthus.ambeth.util.IPrefetchHelper</code>
	 * 
	 * @return
	 */
	@Deprecated
	IPrefetchConfig createPrefetch();

	/**
	 * Please use <code>de.osthus.ambeth.util.IPrefetchHelper</code>
	 * 
	 * @return
	 */
	@Deprecated
	IPrefetchState prefetch(Object objects, IMap<Class<?>, List<String>> typeToMembersToInitialize);

	/**
	 * Please use <code>de.osthus.ambeth.util.IPrefetchHelper</code>
	 * 
	 * @return
	 */
	@Deprecated
	IPrefetchState prefetch(Object objects);

	Object createInstanceOfTargetExpectedType(Class<?> expectedType, Class<?> elementType);

	Object convertResultListToExpectedType(List<Object> resultList, Class<?> expectedType, Class<?> elementType);

	Object[] extractPrimitives(IEntityMetaData metaData, Object obj);

	IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj);

	/**
	 * 
	 * @param metaData
	 * @param obj
	 * @param relationValues
	 *            out param
	 * @return
	 */
	IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj, List<Object> relationValues);
}
