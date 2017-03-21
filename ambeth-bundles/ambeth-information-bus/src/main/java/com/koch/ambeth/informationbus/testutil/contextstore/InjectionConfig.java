package com.koch.ambeth.informationbus.testutil.contextstore;

/*-
 * #%L
 * jambeth-information-bus
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

import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;

public class InjectionConfig implements IInjectionConfig, IInjectNeedBean, IInjectNeedTargetBean, IInjectNeedTargetContext, IInjectNeedTargetProperty
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private IBeanGetter sourceBean;

	private IBeanGetter targetBean;

	private String propertyName;

	private String tempContextName;

	@Override
	public void validate()
	{
		ParamChecker.assertNotNull(sourceBean, "sourceBean");
		ParamChecker.assertNotNull(targetBean, "targetBean");
		ParamChecker.assertNull(tempContextName, "tempContextName");
		ParamChecker.assertNotNull(propertyName, "propertyName");
	}

	@Override
	public IBeanGetter getSourceBeanGetter()
	{
		return sourceBean;
	}

	public void setSourceBeanGetter(IBeanGetter beanGetter)
	{
		ParamChecker.assertNull(tempContextName, "Context name is set");
		ParamChecker.assertNull(sourceBean, "Source bean is already set");
		ParamChecker.assertNotNull(beanGetter, "beanGetter");
		sourceBean = beanGetter;
	}

	@Override
	public IBeanGetter getTargetBeanGetter()
	{
		return targetBean;
	}

	public void setTargetBeanGetter(IBeanGetter beanGetter)
	{
		ParamChecker.assertNull(tempContextName, "Context name is set");
		ParamChecker.assertNull(targetBean, "Target bean is already set");
		ParamChecker.assertNotNull(beanGetter, "targetBean");
		targetBean = beanGetter;
	}

	@Override
	public String getTargetPropertyName()
	{
		return propertyName;
	}

	public void setSourceContextName(String contextName)
	{
		checkBeforeSetContextName(contextName);
		tempContextName = contextName;
	}

	@Override
	public IInjectNeedTargetContext bean(String beanName)
	{
		checkBeforeSetBeanName(beanName);
		sourceBean = buildBeanGetter(beanName);

		return this;
	}

	@Override
	public IInjectNeedTargetContext bean(Class<?> beanType)
	{
		checkBeforeSetBeanInterface(beanType);
		sourceBean = buildBeanGetter(beanType);

		return this;
	}

	@Override
	public IInjectNeedTargetContext bean(IBeanConfiguration beanConfig)
	{
		checkBeforeSetBeanConfig(beanConfig);
		String beanName = beanConfig.getName();
		bean(beanName);

		return this;
	}

	@Override
	public IInjectNeedTargetBean in(String contextName)
	{
		checkBeforeSetContextName(contextName);
		tempContextName = contextName;

		return this;
	}

	@Override
	public IInjectNeedTargetProperty intoBean(String beanName)
	{
		checkBeforeSetBeanName(beanName);
		targetBean = buildBeanGetter(beanName);

		return this;
	}

	@Override
	public IInjectNeedTargetProperty intoBean(Class<?> beanType)
	{
		checkBeforeSetBeanInterface(beanType);
		targetBean = buildBeanGetter(beanType);

		return this;
	}

	@Override
	public IInjectNeedTargetProperty intoBean(IBeanConfiguration beanConfig)
	{
		checkBeforeSetBeanConfig(beanConfig);
		String beanName = beanConfig.getName();
		intoBean(beanName);

		return this;
	}

	@Override
	public IInjectNeedTargetProperty intoBean(IBeanGetter beanGetter)
	{
		setTargetBeanGetter(beanGetter);
		return this;
	}

	@Override
	public IInjectNeedTargetProperty into(Object bean)
	{
		ParamChecker.assertNotNull(bean, "bean");
		DirectBeanGetter beanGetter = new DirectBeanGetter();
		beanGetter.setBean(bean);
		intoBean(beanGetter);

		return this;
	}

	@Override
	public void property(String propertyName)
	{
		this.propertyName = propertyName;
		validate();
	}

	private void checkBeforeSetContextName(String contextName)
	{
		ParamChecker.assertNull(tempContextName, "Context name already set");
		ParamChecker.assertNotNull(contextName, "Context name is null");
		ParamChecker.assertFalse(contextName.isEmpty(), "Context name is empty");
	}

	private void checkBeforeSetBeanName(String beanName)
	{
		ParamChecker.assertNotNull(tempContextName, "Context name not set");
		ParamChecker.assertNotNull(beanName, "Bean name is null");
		ParamChecker.assertFalse(beanName.isEmpty(), "Bean name is empty");
	}

	private void checkBeforeSetBeanInterface(Class<?> beanInterface)
	{
		ParamChecker.assertNotNull(tempContextName, "Context name not set");
		ParamChecker.assertNotNull(beanInterface, "Bean interface is null");
	}

	private void checkBeforeSetBeanConfig(IBeanConfiguration beanConfig)
	{
		ParamChecker.assertNotNull(tempContextName, "Context name not set");
		ParamChecker.assertNotNull(beanConfig, "Bean configuration is null");
	}

	private IBeanGetter buildBeanGetter(String beanName)
	{
		BeanByNameGetter beanGetter = new BeanByNameGetter();
		beanGetter.setContextName(tempContextName);
		beanGetter.setBeanName(beanName);
		tempContextName = null;
		return beanGetter;
	}

	private IBeanGetter buildBeanGetter(Class<?> beanType)
	{
		BeanByAutowiringGetter beanGetter = new BeanByAutowiringGetter();
		beanGetter.setContextName(tempContextName);
		beanGetter.setBeanType(beanType);
		tempContextName = null;
		return beanGetter;
	}

}
