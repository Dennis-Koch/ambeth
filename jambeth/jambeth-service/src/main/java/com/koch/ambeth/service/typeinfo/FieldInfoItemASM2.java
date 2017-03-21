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

import com.koch.ambeth.ioc.accessor.AbstractAccessor;

public class FieldInfoItemASM2 extends FieldInfoItem
{
	protected final AbstractAccessor accessor;

	public FieldInfoItemASM2(Field field, AbstractAccessor accessor)
	{
		this(field, true, accessor);
	}

	public FieldInfoItemASM2(Field field, boolean allowNullEquivalentValue, AbstractAccessor accessor)
	{
		this(field, allowNullEquivalentValue, field.getName(), accessor);
	}

	public FieldInfoItemASM2(Field field, String propertyName, AbstractAccessor accessor)
	{
		this(field, true, propertyName, accessor);
	}

	public FieldInfoItemASM2(Field field, boolean allowNullEquivalentValue, String propertyName, AbstractAccessor accessor)
	{
		super(field, allowNullEquivalentValue, propertyName);
		this.accessor = accessor;
	}

	@Override
	public Object getValue(Object obj, boolean allowNullEquivalentValue)
	{
		Object value = accessor.getValue(obj);
		Object nullEquivalentValue = this.nullEquivalentValue;
		if (nullEquivalentValue != null && nullEquivalentValue.equals(value))
		{
			if (allowNullEquivalentValue)
			{
				return nullEquivalentValue;
			}
			return null;
		}
		return value;
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		if (value == null && allowNullEquivalentValue)
		{
			value = nullEquivalentValue;
		}
		accessor.setValue(obj, value);
	}
}
