package de.osthus.ambeth.ioc;

import java.util.List;
import java.util.Set;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.config.IPropertyConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.typeinfo.IPropertyInfo;

public interface IBeanPreProcessor
{
	void preProcessProperties(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IProperties props, String beanName, Object service,
			Class<?> beanType, List<IPropertyConfiguration> propertyConfigs, Set<String> ignoredPropertyNames, IPropertyInfo[] properties);
}
