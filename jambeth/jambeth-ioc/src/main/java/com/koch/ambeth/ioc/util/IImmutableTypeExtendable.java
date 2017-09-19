package com.koch.ambeth.ioc.util;

public interface IImmutableTypeExtendable {
	void registerImmutableType(Class<?> immutableType);

	void unregisterImmutableType(Class<?> immutableType);
}
