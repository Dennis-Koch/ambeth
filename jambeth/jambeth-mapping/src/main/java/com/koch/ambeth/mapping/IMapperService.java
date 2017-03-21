package com.koch.ambeth.mapping;

/*-
 * #%L
 * jambeth-mapping
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.List;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.collections.IList;

public interface IMapperService extends IDisposable, AutoCloseable {
	<T> T mapToBusinessObject(Object valueObject);

	Object getMappedBusinessObject(IObjRef objRef);

	<T> T mapToBusinessObjectListFromListType(Object listTypeObject);

	<T> T mapToBusinessObjectList(List<?> valueObjectList);

	<T> T mapToValueObject(Object businessObject, Class<T> valueObjectType);

	<L> L mapToValueObjectListType(List<?> businessObjectList, Class<?> valueObjectType,
			Class<L> listType);

	<L> L mapToValueObjectRefListType(List<?> businessObjectList, Class<L> valueObjectRefListType);

	<T> T mapToValueObjectList(List<?> businessObjectList, Class<?> valueObjectType);

	Object getIdFromValueObject(Object valueObject);

	Object getVersionFromValueObject(Object valueObject);

	IList<Object> getAllActiveBusinessObjects();
}
