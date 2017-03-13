package com.koch.ambeth.mapping;

import java.util.List;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.collections.IList;

public interface IMapperService extends IDisposable
{
	<T> T mapToBusinessObject(Object valueObject);

	Object getMappedBusinessObject(IObjRef objRef);

	<T> T mapToBusinessObjectListFromListType(Object listTypeObject);

	<T> T mapToBusinessObjectList(List<?> valueObjectList);

	<T> T mapToValueObject(Object businessObject, Class<T> valueObjectType);

	<L> L mapToValueObjectListType(List<?> businessObjectList, Class<?> valueObjectType, Class<L> listType);

	<L> L mapToValueObjectRefListType(List<?> businessObjectList, Class<L> valueObjectRefListType);

	<T> T mapToValueObjectList(List<?> businessObjectList, Class<?> valueObjectType);

	Object getIdFromValueObject(Object valueObject);

	Object getVersionFromValueObject(Object valueObject);

	IList<Object> getAllActiveBusinessObjects();
}
