package de.osthus.ambeth.typeinfo;

import de.osthus.ambeth.collections.HashMap;

public final class NullEquivalentValueUtil
{
	private static final HashMap<Class<?>, Object> nullEquivalentValues = new HashMap<Class<?>, Object>(0.5f);

	static
	{
		nullEquivalentValues.put(Boolean.TYPE, Boolean.valueOf(false));
		nullEquivalentValues.put(Double.TYPE, Double.valueOf(0));
		nullEquivalentValues.put(Long.TYPE, Long.valueOf(0));
		nullEquivalentValues.put(Float.TYPE, Float.valueOf(0));
		nullEquivalentValues.put(Integer.TYPE, Integer.valueOf(0));
		nullEquivalentValues.put(Short.TYPE, Short.valueOf((short) 0));
		nullEquivalentValues.put(Byte.TYPE, Byte.valueOf((byte) 0));
		nullEquivalentValues.put(Character.TYPE, Character.valueOf('\0'));
		nullEquivalentValues.put(Boolean.class, Boolean.valueOf(false));
		nullEquivalentValues.put(Double.class, Double.valueOf(0));
		nullEquivalentValues.put(Long.class, Long.valueOf(0));
		nullEquivalentValues.put(Float.class, Float.valueOf(0));
		nullEquivalentValues.put(Integer.class, Integer.valueOf(0));
		nullEquivalentValues.put(Short.class, Short.valueOf((short) 0));
		nullEquivalentValues.put(Byte.class, Byte.valueOf((byte) 0));
		nullEquivalentValues.put(Character.class, Character.valueOf('\0'));
	}

	public static Object getNullEquivalentValue(Class<?> type)
	{
		return nullEquivalentValues.get(type);
	}

	private NullEquivalentValueUtil()
	{
		// intended blank
	}
}
