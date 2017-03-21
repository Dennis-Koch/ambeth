package com.koch.ambeth.util.typeinfo;

/*-
 * #%L
 * jambeth-util
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public interface IPropertyInfo
{
	Class<?> getEntityType();

	String getName();

	Class<?> getPropertyType();

	Class<?> getDeclaringType();

	Class<?> getElementType();

	boolean isReadable();

	boolean isWritable();

	Field getBackingField();

	int getModifiers();

	Object getValue(Object obj);

	void setValue(Object obj, Object value);

	Annotation[] getAnnotations();

	<V extends Annotation> V getAnnotation(Class<V> annotationType);

	<V extends Annotation> boolean isAnnotationPresent(Class<V> annotationType);
}
