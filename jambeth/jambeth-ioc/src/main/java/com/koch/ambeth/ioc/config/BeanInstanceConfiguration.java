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

import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.config.IProperties;

public class BeanInstanceConfiguration extends AbstractBeanConfiguration
{
	protected Object bean;

	protected boolean withLifecycle;

	public BeanInstanceConfiguration(Object bean, String beanName, boolean withLifecycle, IProperties props)
	{
		super(beanName, props);
		ParamChecker.assertParamNotNull(bean, "bean");
		this.bean = bean;
		this.withLifecycle = withLifecycle;
		if (withLifecycle && declarationStackTrace != null && bean instanceof IDeclarationStackTraceAware)
		{
			((IDeclarationStackTraceAware) bean).setDeclarationStackTrace(declarationStackTrace);
		}
	}

	@Override
	public Class<?> getBeanType()
	{
		return bean.getClass();
	}

	@Override
	public Object getInstance()
	{
		return bean;
	}

	@Override
	public Object getInstance(Class<?> instanceType)
	{
		return bean;
	}

	@Override
	public boolean isWithLifecycle()
	{
		return withLifecycle;
	}
}
