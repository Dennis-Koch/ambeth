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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Map;

public final class TypeInfoItemUtil
{
	public static Class<?> getElementTypeUsingReflection(Class<?> propertyType, Type genericType)
	{
		if (propertyType == null)
		{
			if (genericType instanceof Class<?>)
			{
				return (Class<?>) genericType;
			}
			else if (genericType instanceof TypeVariable<?>)
			{
				return Object.class;
			}
			else if (genericType instanceof ParameterizedType)
			{
				return getElementTypeUsingReflection((Class<?>) ((ParameterizedType) genericType).getRawType(), genericType);
			}
			else if (genericType instanceof WildcardType)
			{
				return getElementTypeUsingReflection(propertyType, ((WildcardType) genericType).getUpperBounds()[0]);
			}
			return propertyType;
		}
		if (propertyType.isArray())
		{
			return propertyType.getComponentType();
		}
		else if (Collection.class.isAssignableFrom(propertyType))
		{
			if (!(genericType instanceof ParameterizedType))
			{
				if (genericType instanceof Class)
				{
					return (Class<?>) genericType;
				}
				return Object.class;
			}
			ParameterizedType castedType = (ParameterizedType) genericType;
			Type[] actualTypeArguments = castedType.getActualTypeArguments();
			if (actualTypeArguments.length == 1)
			{
				Type actualType = actualTypeArguments[0];
				if (actualType instanceof ParameterizedType)
				{
					propertyType = (Class<?>) ((ParameterizedType) actualType).getRawType();
				}
				else if (actualType instanceof TypeVariable)
				{
					return Object.class;
				}
				else if (actualType instanceof WildcardType)
				{
					Type[] upperBounds = ((WildcardType) actualType).getUpperBounds();
					return getElementTypeUsingReflection(propertyType, upperBounds[0]);
				}
				else
				{
					propertyType = (Class<?>) actualType;
				}
			}
			else if (actualTypeArguments.length == 0)
			{
				// TODO This should be logged, but no logger is available
			}
			else
			{
				throw new IllegalArgumentException("Properties with more than one generic type are not supported");
			}
		}
		else if (Map.class.isAssignableFrom(propertyType))
		{
			if (!(genericType instanceof ParameterizedType))
			{
				if (genericType instanceof Class)
				{
					return (Class<?>) genericType;
				}
				return Object.class;
			}
			ParameterizedType castedType = (ParameterizedType) genericType;
			Type[] actualTypeArguments = castedType.getActualTypeArguments();
			return getElementTypeUsingReflection(null, actualTypeArguments[1]);
		}
		return propertyType;
	}

	private TypeInfoItemUtil()
	{
		// Intended blank
	}
}
