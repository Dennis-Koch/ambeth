package de.osthus.ambeth.util;

import java.util.List;

import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;

public interface ICacheHelper
{
	Object createInstanceOfTargetExpectedType(Class<?> expectedType, Class<?> elementType);

	Object convertResultListToExpectedType(List<Object> resultList, Class<?> expectedType, Class<?> elementType);

	Object[] extractPrimitives(IEntityMetaData metaData, Object obj);

	IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj);

	/**
	 * 
	 * @param metaData
	 *            Meta data of obj
	 * @param obj
	 *            Entity to extract the relations from
	 * @param relationValues
	 *            Actual relation values (out param)
	 * @return ORI array for the relations
	 */
	IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj, List<Object> relationValues);
}
