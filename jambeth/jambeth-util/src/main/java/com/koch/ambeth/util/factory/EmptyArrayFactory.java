package com.koch.ambeth.util.factory;

import java.lang.reflect.Array;
import java.util.concurrent.ConcurrentHashMap;

public class EmptyArrayFactory extends IEmptyArrayFactory {
	protected final ConcurrentHashMap<Class<?>, Object> typeToEmptyArray =
			new ConcurrentHashMap<>(128, 0.5f);

	@Override
	public <T> Object createSharedEmptyArray(Class<T> componentType) {
		Object array = typeToEmptyArray.get(componentType);
		if (array != null) {
			return array;
		}
		array = Array.newInstance(componentType, 0);
		if (typeToEmptyArray.putIfAbsent(componentType, array) != null) {
			// concurrent thread might have been faster
			array = typeToEmptyArray.get(componentType);
		}
		return array;
	}
}
