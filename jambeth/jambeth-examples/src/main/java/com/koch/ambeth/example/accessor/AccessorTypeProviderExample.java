package com.koch.ambeth.example.accessor;

/*-
 * #%L
 * jambeth-examples
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.accessor.AbstractAccessor;
import com.koch.ambeth.ioc.accessor.IAccessorTypeProvider;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class AccessorTypeProviderExample implements IInitializingBean {
	public static abstract class ExampleConstructorDelegate {
		public abstract Object create(int arg1);

		public abstract Object create(long arg1);
	}

	public static class ExampleObjectType {
		protected int exampleIntValue;

		protected long exampleLongValue;

		public ExampleObjectType(int exampleIntValue) {
			this.exampleIntValue = exampleIntValue;
		}

		public ExampleObjectType(long exampleLongValue) {
			this.exampleLongValue = exampleLongValue;
		}

		public int getExampleIntValue() {
			return exampleIntValue;
		}

		public long getExampleLongValue() {
			return exampleLongValue;
		}
	}

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	protected ExampleConstructorDelegate constructorDelegate;

	protected AbstractAccessor exampleIntAccessor, exampleLongAccessor;

	@Override
	public void afterPropertiesSet() throws Throwable {
		Class<?> objectType = ExampleObjectType.class;
		constructorDelegate =
				accessorTypeProvider.getConstructorType(ExampleConstructorDelegate.class, objectType);
		exampleIntAccessor = accessorTypeProvider.getAccessorType(objectType,
				propertyInfoProvider.getProperty(objectType, "ExampleIntValue"));
		exampleLongAccessor = accessorTypeProvider.getAccessorType(objectType,
				propertyInfoProvider.getProperty(objectType, "ExampleLongValue"));
	}

	public void exampleInstantiation() {
		Object obj1 = constructorDelegate.create(1);
		Object obj2 = constructorDelegate.create(2L);
		exampleIntAccessor.getValue(obj1);
		exampleLongAccessor.getValue(obj2);
	}
}
