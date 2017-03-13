package com.koch.ambeth.cache.proxy;

public interface IEntityEquals
{
	Object get__Id();

	Class<?> get__BaseType();

	@Override
	boolean equals(Object obj);

	@Override
	int hashCode();

	@Override
	String toString();
}
