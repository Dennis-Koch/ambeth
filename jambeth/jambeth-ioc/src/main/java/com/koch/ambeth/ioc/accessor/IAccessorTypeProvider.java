package com.koch.ambeth.ioc.accessor;

import com.koch.ambeth.util.typeinfo.IPropertyInfo;

public interface IAccessorTypeProvider
{
	AbstractAccessor getAccessorType(Class<?> type, IPropertyInfo property);

	<V> V getConstructorType(Class<V> delegateType, Class<?> targetType);
}
