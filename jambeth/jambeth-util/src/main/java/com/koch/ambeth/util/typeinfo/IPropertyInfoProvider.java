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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.koch.ambeth.util.collections.IMap;

public interface IPropertyInfoProvider {
	IPropertyInfo getProperty(Object obj, String propertyName);

	IPropertyInfo getProperty(Class<?> type, String propertyName);

	IPropertyInfo[] getProperties(Object obj);

	/**
	 * Returns a PropertyInfo array with all public declared properties of this class.
	 *
	 * @param type
	 * @return
	 */
	IPropertyInfo[] getProperties(Class<?> type);

	IPropertyInfo[] getIocProperties(Class<?> beanType);

	IPropertyInfo[] getPrivateProperties(Class<?> type);

	IMap<String, IPropertyInfo> getPropertyMap(Object obj);

	/**
	 * Returns a property name to PropertyInfo Map with all public declared properties of this class.
	 *
	 * @param type
	 * @return
	 */
	IMap<String, IPropertyInfo> getPropertyMap(Class<?> type);

	IMap<String, IPropertyInfo> getIocPropertyMap(Class<?> type);

	IMap<String, IPropertyInfo> getPrivatePropertyMap(Class<?> type);

	String getPropertyNameFor(Method method);

	String getPropertyNameFor(Field field);
}
