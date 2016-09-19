package de.osthus.ambeth.util;

import de.osthus.ambeth.collections.HashMap;

public final class WrapperTypeSet
{
	private static final HashMap<Class<?>, Class<?>> wrapperTypesMap = new HashMap<Class<?>, Class<?>>(0.5f);

	static
	{
		wrapperTypesMap.put(Integer.class, Integer.TYPE);
		wrapperTypesMap.put(Long.class, Long.TYPE);
		wrapperTypesMap.put(Double.class, Double.TYPE);
		wrapperTypesMap.put(Float.class, Float.TYPE);
		wrapperTypesMap.put(Short.class, Short.TYPE);
		wrapperTypesMap.put(Character.class, Character.TYPE);
		wrapperTypesMap.put(Byte.class, Byte.TYPE);
		wrapperTypesMap.put(Boolean.class, Boolean.TYPE);
	}

	private WrapperTypeSet()
	{
		// Intended blank
	}

	public static Class<?> getUnwrappedType(Class<?> wrapperType)
	{
		return wrapperTypesMap.get(wrapperType);
	}
}
