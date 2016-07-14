package de.osthus.ambeth.query.shuang;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class GenericTypeUtils
{
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

	public static List<Type> getAllGnericParam(Object obj)
	{
		List<Type> result = new ArrayList<Type>();
		for (ParameterizedType parameterizedType : getAllParameterizedType(obj))
		{
			result.addAll(Arrays.asList(parameterizedType.getActualTypeArguments()));
		}
		return result;
	}

	public static List<ParameterizedType> getAllParameterizedType(Object obj)
	{
		List<ParameterizedType> result = new ArrayList<ParameterizedType>();
		Class<? extends Object> clazz = obj.getClass();
		while (clazz != Object.class)
		{
			Type type = clazz.getGenericSuperclass();
			if (type instanceof ParameterizedType)
			{
				result.add((ParameterizedType) type);
			}
			clazz = clazz.getSuperclass();
		}
		List<Type> interfaces = getAllInterfaces(obj.getClass());
		for (Type type : interfaces)
		{
			if (type instanceof ParameterizedType)
			{
				result.add((ParameterizedType) type);
			}
		}
		return result;
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
