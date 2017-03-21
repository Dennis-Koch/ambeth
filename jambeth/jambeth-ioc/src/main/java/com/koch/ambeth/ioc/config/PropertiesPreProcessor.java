package com.koch.ambeth.ioc.config;

/*-
 * #%L
 * jambeth-ioc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.List;
import java.util.Set;

import com.koch.ambeth.ioc.IBeanPreProcessor;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.exception.BeanContextInitException;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class PropertiesPreProcessor implements IBeanPreProcessor, IInitializingBean {
	@LogInstance
	private ILogger log;

	protected IPropertyInfoProvider propertyInfoProvider;

	protected IConversionHelper conversionHelper;

	@Override
	public void afterPropertiesSet() {
		ParamChecker.assertNotNull(propertyInfoProvider, "propertyInfoProvider");
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
	}

	public void setPropertyInfoProvider(IPropertyInfoProvider propertyInfoProvider) {
		this.propertyInfoProvider = propertyInfoProvider;
	}

	public void setConversionHelper(IConversionHelper conversionHelper) {
		this.conversionHelper = conversionHelper;
	}

	@Override
	public void preProcessProperties(IBeanContextFactory beanContextFactory,
			IServiceContext beanContext, IProperties props, String beanName, Object service,
			Class<?> beanType, List<IPropertyConfiguration> propertyConfigs,
			Set<String> ignoredPropertyNames, IPropertyInfo[] properties) {
		if (properties == null) {
			properties = propertyInfoProvider.getProperties(service.getClass());
		}
		for (IPropertyInfo prop : properties) {
			if (!prop.isWritable()) {
				continue;
			}
			Property propertyAttribute = prop.getAnnotation(Property.class);
			if (propertyAttribute == null) {
				continue;
			}
			if (ignoredPropertyNames.contains(prop.getName())) {
				// do not handle this property
				continue;
			}
			if (Property.DEFAULT_VALUE.equals(propertyAttribute.name())
					&& Property.DEFAULT_VALUE.equals(propertyAttribute.defaultValue())) {
				if (propertyAttribute.mandatory()) {
					String propName = prop.getName();
					boolean propertyInitialized = false;
					// check if the mandatory property field has been initialized with a value
					for (int a = propertyConfigs.size(); a-- > 0;) {
						IPropertyConfiguration propertyConfig = propertyConfigs.get(a);
						if (propName.equals(propertyConfig.getPropertyName())) {
							propertyInitialized = true;
							break;
						}
					}
					if (!propertyInitialized) {
						throw new IllegalStateException(
								"Mandatory property '" + propName + "' not initialized");
					}
				}
				continue;
			}
			Object value = props.get(propertyAttribute.name());

			if (value == null) {
				String stringValue = propertyAttribute.defaultValue();
				if (Property.DEFAULT_VALUE.equals(stringValue)) {
					if (propertyAttribute.mandatory()) {
						throw new BeanContextInitException("Could not resolve mandatory environment property '"
								+ propertyAttribute.name() + "'");
					}
					else {
						continue;
					}
				}
				else {
					value = props.resolvePropertyParts(stringValue);
				}
			}
			value = conversionHelper.convertValueToType(prop.getPropertyType(), value);
			prop.setValue(service, value);
		}
	}
}
