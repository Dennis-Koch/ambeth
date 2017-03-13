package com.koch.ambeth.util.factory;

public interface IFactory<T>
{

	public static final Object[] emptyArgs = new Object[0];

	<V> V create(Class<V> requestedType, Object... args);

}
