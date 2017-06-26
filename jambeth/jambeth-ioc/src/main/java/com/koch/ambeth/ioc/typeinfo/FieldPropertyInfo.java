package com.koch.ambeth.ioc.typeinfo;

import java.beans.Introspector;

/*-
 * #%L
 * jambeth-ioc
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

import java.lang.reflect.Field;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class FieldPropertyInfo extends AbstractPropertyInfo {
	public FieldPropertyInfo(Class<?> entityType, String propertyName, Field field) {
		this(entityType, propertyName, field, null);
	}

	public FieldPropertyInfo(Class<?> entityType, String propertyName, Field field,
			IThreadLocalObjectCollector objectCollector) {
		super(entityType, objectCollector);
		field.setAccessible(true);
		backingField = field;
		modifiers = field.getModifiers();
		name = propertyName;
		nameForJavaBeans = Introspector.decapitalize(propertyName);
		declaringType = field.getDeclaringClass();
		propertyType = field.getType();
		elementType =
				TypeInfoItemUtil.getElementTypeUsingReflection(propertyType, field.getGenericType());
		init(objectCollector);
	}

	@Override
	protected void init(IThreadLocalObjectCollector objectCollector) {
		putAnnotations(backingField);
		super.init(objectCollector);
	}

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public Object getValue(Object obj) {
		try {
			return backingField.get(obj);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void setValue(Object obj, Object value) {
		try {
			backingField.set(obj, value);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
