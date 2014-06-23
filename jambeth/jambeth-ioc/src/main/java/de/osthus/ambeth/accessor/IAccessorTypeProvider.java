package de.osthus.ambeth.accessor;

public interface IAccessorTypeProvider
{
	AbstractAccessor getAccessorType(Class<?> type, String propertyName, Class<?> propertyType);

	<V> V getConstructorType(Class<V> delegateType, Class<?> targetType);
}
