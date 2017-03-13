package com.koch.ambeth.ioc;

import java.util.List;
import java.util.Set;

import com.koch.ambeth.ioc.config.IPropertyConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;

public interface IBeanPreProcessor
{
	void preProcessProperties(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IProperties props, String beanName, Object service,
			Class<?> beanType, List<IPropertyConfiguration> propertyConfigs, Set<String> ignoredPropertyNames, IPropertyInfo[] properties);
}
