package de.osthus.ambeth.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;

public final class ImmutableTypeSet
{
	protected static final HashSet<Class<?>> valueTypeSet = new HashSet<Class<?>>(0.5f);

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

		valueTypeSet.add(Integer.class);
		valueTypeSet.add(Integer.TYPE);
		valueTypeSet.add(Long.class);
		valueTypeSet.add(Long.TYPE);
		valueTypeSet.add(Double.class);
		valueTypeSet.add(Double.TYPE);
		valueTypeSet.add(Float.class);
		valueTypeSet.add(Float.TYPE);
		valueTypeSet.add(Short.class);
		valueTypeSet.add(Short.TYPE);
		valueTypeSet.add(Character.class);
		valueTypeSet.add(Character.TYPE);
		valueTypeSet.add(Byte.class);
		valueTypeSet.add(Byte.TYPE);
		valueTypeSet.add(Boolean.class);
		valueTypeSet.add(Boolean.TYPE);
		valueTypeSet.add(String.class);
		valueTypeSet.add(Class.class);
		valueTypeSet.add(void.class);
		valueTypeSet.add(BigInteger.class);
		valueTypeSet.add(BigDecimal.class);
	}

	public static void addImmutableTypesTo(Collection<Class<?>> collection)
	{
		collection.addAll(valueTypeSet);
	}

	public static boolean isImmutableType(Class<?> type)
	{
		return type.isPrimitive() || type.isEnum() || valueTypeSet.contains(type) || IImmutableType.class.isAssignableFrom(type);
	}

	private ImmutableTypeSet()
	{
		// Intended blank
	}

	public static Class<?> getUnwrappedType(Class<?> wrapperType)
	{
		return wrapperTypesMap.get(wrapperType);
	}
}
