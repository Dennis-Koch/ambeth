package de.osthus.ambeth.mapping;

import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.util.IDisposable;

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
