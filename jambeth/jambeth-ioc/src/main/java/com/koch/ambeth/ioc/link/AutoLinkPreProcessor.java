package com.koch.ambeth.ioc.link;

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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.ioc.IBeanPreProcessor;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IPropertyConfiguration;
import com.koch.ambeth.ioc.extendable.IExtendableRegistry;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;

public class AutoLinkPreProcessor implements IInitializingBean, IBeanPreProcessor {
	@LogInstance
	private ILogger log;

	protected IExtendableRegistry extendableRegistry;

	protected Class<?> extensionType;

	protected String extendableName;

	protected Class<?> extendableType;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(extendableType, "extendableType");
		if (extensionType == null) {
			ParamChecker.assertNotNull(extendableRegistry, "extendableRegistry");
			Method[] addRemoveMethods = extendableRegistry.getAddRemoveMethods(extendableType);
			extensionType = addRemoveMethods[0].getParameterTypes()[0];
		}
	}

	public void setExtendableName(String extendableName) {
		this.extendableName = extendableName;
	}

	public void setExtendableRegistry(IExtendableRegistry extendableRegistry) {
		this.extendableRegistry = extendableRegistry;
	}

	public void setExtendableType(Class<?> extendableType) {
		this.extendableType = extendableType;
	}

	public void setExtensionType(Class<?> extensionType) {
		this.extensionType = extensionType;
	}

	@Override
	public void preProcessProperties(IBeanContextFactory beanContextFactory,
			IServiceContext beanContext, IProperties props, String beanName, Object service,
			Class<?> beanType, List<IPropertyConfiguration> propertyConfigs,
			Set<String> ignoredPropertyNames, IPropertyInfo[] properties) {
		if (extensionType.isAssignableFrom(service.getClass())) {
			if (log.isDebugEnabled()) {
				if (extendableName == null) {
					log.debug("Registering bean '" + beanName + "' to " + extendableType.getSimpleName()
							+ " because it implements " + extensionType.getSimpleName());
				}
				else {
					log.debug(
							"Registering bean '" + beanName + "' to " + extendableType.getSimpleName() + " ('"
									+ extendableName + "') because it implements " + extensionType.getSimpleName());
				}
			}
			ILinkRegistryNeededConfiguration<Object> link = beanContextFactory.link(service);
			if (extendableName == null) {
				link.to(extendableType);
			}
			else {
				link.to(extendableName, extendableType);
			}
		}
	}
}
