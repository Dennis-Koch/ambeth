package de.osthus.ambeth.util;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;

public final class ImmutableTypeSet
{
	protected static final HashSet<Class<?>> immutableTypeSet = new HashSet<Class<?>>(0.5f);

	private static final HashMap<Class<?>, Class<?>> wrapperTypesMap = new HashMap<Class<?>, Class<?>>(0.5f);

	private static final ClassExtendableContainer<Class<?>> immutableSuperTypes = new ClassExtendableContainer<Class<?>>("", "");

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

		immutableTypeSet.add(Integer.class);
		immutableTypeSet.add(Integer.TYPE);
		immutableTypeSet.add(Long.class);
		immutableTypeSet.add(Long.TYPE);
		immutableTypeSet.add(Double.class);
		immutableTypeSet.add(Double.TYPE);
		immutableTypeSet.add(Float.class);
		immutableTypeSet.add(Float.TYPE);
		immutableTypeSet.add(Short.class);
		immutableTypeSet.add(Short.TYPE);
		immutableTypeSet.add(Character.class);
		immutableTypeSet.add(Character.TYPE);
		immutableTypeSet.add(Byte.class);
		immutableTypeSet.add(Byte.TYPE);
		immutableTypeSet.add(Boolean.class);
		immutableTypeSet.add(Boolean.TYPE);
		immutableTypeSet.add(String.class);
		immutableTypeSet.add(Class.class);
		immutableTypeSet.add(void.class);
		immutableTypeSet.add(BigInteger.class);
		immutableTypeSet.add(BigDecimal.class);

		immutableTypeSet.add(Pattern.class);
		immutableTypeSet.add(URI.class);
		immutableTypeSet.add(URL.class);
		immutableTypeSet.add(File.class);

		immutableSuperTypes.register(Charset.class, Charset.class);
	}

	public static void addImmutableTypesTo(Collection<Class<?>> collection)
	{
		collection.addAll(immutableTypeSet);
	}

	public static boolean isImmutableType(Class<?> type)
	{
		return type.isPrimitive() || type.isEnum() || immutableTypeSet.contains(type) || IImmutableType.class.isAssignableFrom(type)
				|| immutableSuperTypes.getExtension(type) != null;
	}

	public static void flushState()
	{
		immutableSuperTypes.clearWeakCache();
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
