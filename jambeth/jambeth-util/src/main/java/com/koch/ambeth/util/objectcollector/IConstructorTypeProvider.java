package com.koch.ambeth.util.objectcollector;

public interface IConstructorTypeProvider {
	<V> V getConstructorType(Class<V> delegateType, Class<?> targetType);
}