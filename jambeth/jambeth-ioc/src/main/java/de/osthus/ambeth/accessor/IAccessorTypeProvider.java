package de.osthus.ambeth.accessor;

import de.osthus.ambeth.typeinfo.IPropertyInfo;

public interface IAccessorTypeProvider
{
	AbstractAccessor getAccessorType(Class<?> type, IPropertyInfo property);

	<V> V getConstructorType(Class<V> delegateType, Class<?> targetType);
}
