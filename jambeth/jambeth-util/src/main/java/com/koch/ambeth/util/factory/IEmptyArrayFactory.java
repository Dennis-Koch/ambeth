package com.koch.ambeth.util.factory;

public abstract class IEmptyArrayFactory {
	public abstract <T> Object createSharedEmptyArray(Class<T> componentType);
}
