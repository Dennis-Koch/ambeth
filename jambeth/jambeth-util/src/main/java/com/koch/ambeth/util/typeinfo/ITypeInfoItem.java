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
import java.util.Collection;

import com.koch.ambeth.util.INamed;

public interface ITypeInfoItem extends INamed {
	Object getDefaultValue();

	void setDefaultValue(Object defaultValue);

	Object getNullEquivalentValue();

	void setNullEquivalentValue(Object nullEquivalentValue);

	Class<?> getRealType();

	Class<?> getElementType();

	Class<?> getDeclaringType();

	boolean canRead();

	boolean canWrite();

	boolean isTechnicalMember();

	void setTechnicalMember(boolean b);

	Object getValue(Object obj);

	Object getValue(Object obj, boolean allowNullEquivalentValue);

	void setValue(Object obj, Object value);

	<V extends Annotation> V getAnnotation(Class<V> annotationType);

	String getXMLName();

	boolean isXMLIgnore();

	Collection<?> createInstanceOfCollection();
}
