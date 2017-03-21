package com.koch.ambeth.query.squery;

/*-
 * #%L
 * jambeth-query
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.koch.ambeth.util.collections.LinkedHashSet;

public final class GenericTypeUtils
{
	private GenericTypeUtils()
	{
	}

	/**
	 * obj instanceof rawType, and rawType is a generic interface or class, this method get the Type parameter asign to rawType
	 * 
	 * @param obj
	 *            need to retrive the Type parameter
	 * @param rawType
	 *            the generic interface or class that obj instanceof
	 * @return Type parameter
	 */
	public static Type[] getGenericParam(Object obj, Type rawType)
	{
		for (ParameterizedType parameterizedType : getAllParameterizedType(obj))
		{
			if (parameterizedType.getRawType() == rawType)
			{
				return parameterizedType.getActualTypeArguments();
			}
		}
		return new Class<?>[0];
	}

	/**
	 * Retrieve all the Type parameter that obj use, include the super class or interfaces
	 * 
	 * @param obj
	 *            is the target of retrive Type parameter
	 * @return the Type parameters
	 */
	public static List<Type> getAllGenericParam(Object obj)
	{
		List<Type> result = new ArrayList<Type>();
		for (ParameterizedType parameterizedType : getAllParameterizedType(obj))
		{
			result.addAll(Arrays.asList(parameterizedType.getActualTypeArguments()));
		}
		return result;
	}

	/**
	 * Retrieve all the generic super class or interfaces
	 * 
	 * @param obj
	 *            target to retrieve
	 * @return the ParameterizedType that obj instanceof
	 */
	public static List<ParameterizedType> getAllParameterizedType(Object obj)
	{
		Class<? extends Object> clazz = obj.getClass();
		LinkedHashSet<ParameterizedType> result = new LinkedHashSet<ParameterizedType>();
		while (clazz != Object.class)
		{
			List<Type> interfaces = getAllInterfaces(clazz);
			for (Type interfaceType : interfaces)
			{
				if (interfaceType instanceof ParameterizedType)
				{
					result.add((ParameterizedType) interfaceType);
				}
			}
			Type type = clazz.getGenericSuperclass();
			if (type instanceof ParameterizedType)
			{
				result.add((ParameterizedType) type);
			}
			clazz = clazz.getSuperclass();
		}
		return result.toList();
	}

	private static List<Type> getAllInterfaces(Type... types)
	{
		List<Type> list = new ArrayList<Type>();
		for (Type type : types)
		{
			list.add(type);
			if (type instanceof Class)
			{
				Class<?> clazz = (Class<?>) type;
				list.addAll(getAllInterfaces(clazz.getGenericInterfaces()));
			}
		}
		return list;
	}
}
