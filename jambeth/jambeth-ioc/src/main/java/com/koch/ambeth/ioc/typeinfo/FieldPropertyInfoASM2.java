package com.koch.ambeth.ioc.typeinfo;

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

import com.koch.ambeth.ioc.accessor.AbstractAccessor;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class FieldPropertyInfoASM2 extends FieldPropertyInfo
{
	protected final AbstractAccessor accessor;

	public FieldPropertyInfoASM2(Class<?> entityType, String propertyName, Field field, IThreadLocalObjectCollector objectCollector, AbstractAccessor accessor)
	{
		super(entityType, propertyName, field, objectCollector);
		this.accessor = accessor;
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		accessor.setValue(obj, value);
	}

	@Override
	public Object getValue(Object obj)
	{
		return accessor.getValue(obj);
	}
}
