package com.koch.ambeth.ioc;

import java.util.Set;

import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.BeanContextInit;
import com.koch.ambeth.ioc.factory.BeanContextInitializer;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;

public interface IExternalServiceContext {
	<T> T getServiceByType(Class<T> serviceType);

	boolean initializeAutowiring(BeanContextInit beanContextInit,
			IBeanConfiguration beanConfiguration, Object bean, Class<?> beanType,
			IPropertyInfo[] propertyInfos, Set<String> alreadySpecifiedPropertyNamesSet,
			Set<String> ignoredPropertyNamesSet, BeanContextInitializer beanContextInitializer,
			boolean highPriorityBean, IPropertyInfo prop);
}
