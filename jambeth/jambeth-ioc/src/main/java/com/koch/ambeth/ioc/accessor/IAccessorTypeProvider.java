package com.koch.ambeth.ioc.accessor;

import com.koch.ambeth.util.objectcollector.IConstructorTypeProvider;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;

public interface IAccessorTypeProvider extends IConstructorTypeProvider {
	AbstractAccessor getAccessorType(Class<?> type, IPropertyInfo property);
}
