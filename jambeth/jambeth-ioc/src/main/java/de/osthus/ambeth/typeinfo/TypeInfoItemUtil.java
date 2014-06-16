package de.osthus.ambeth.typeinfo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;

public final class TypeInfoItemUtil
{
	public static Class<?> getElementTypeUsingReflection(Class<?> propertyType, Type genericType)
	{
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

		return propertyType;
	}

	private TypeInfoItemUtil()
	{
		// Intended blank
	}
}
