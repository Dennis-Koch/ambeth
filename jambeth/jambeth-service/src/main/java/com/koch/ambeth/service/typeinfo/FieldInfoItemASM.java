package com.koch.ambeth.service.typeinfo;

/*-
 * #%L
 * jambeth-service
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

import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class FieldInfoItemASM extends FieldInfoItem {
	protected final FieldAccess fieldAccess;

	protected final int fieldIndex;

	public FieldInfoItemASM(Field field, FieldAccess fieldAccess) {
		this(field, true, fieldAccess);
	}

	public FieldInfoItemASM(Field field, boolean allowNullEquivalentValue, FieldAccess fieldAccess) {
		this(field, allowNullEquivalentValue, field.getName(), fieldAccess);
	}

	public FieldInfoItemASM(Field field, String propertyName, FieldAccess fieldAccess) {
		this(field, true, propertyName, fieldAccess);
	}

	public FieldInfoItemASM(Field field, boolean allowNullEquivalentValue, String propertyName,
			FieldAccess fieldAccess) {
		super(field, allowNullEquivalentValue, propertyName);
		this.fieldAccess = fieldAccess;
		fieldIndex = fieldAccess.getIndex(field.getName());
	}

	@Override
	public Object getValue(Object obj, boolean allowNullEquivalentValue) {
		Object value = null;
		try {
			value = fieldAccess.get(obj, fieldIndex);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		Object nullEquivalentValue = this.nullEquivalentValue;
		if (nullEquivalentValue != null && nullEquivalentValue.equals(value)) {
			if (allowNullEquivalentValue) {
				return nullEquivalentValue;
			}
			return null;
		}
		return value;
	}

	@Override
	public void setValue(Object obj, Object value) {
		if (value == null && allowNullEquivalentValue) {
			value = nullEquivalentValue;
		}
		fieldAccess.set(obj, fieldIndex, value);
	}
}
