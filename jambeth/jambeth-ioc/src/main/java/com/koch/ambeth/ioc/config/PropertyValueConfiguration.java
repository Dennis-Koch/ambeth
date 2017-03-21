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

import com.koch.ambeth.util.config.IProperties;

public class PropertyValueConfiguration extends AbstractPropertyConfiguration
{
	protected String propertyName;

	protected Object value;

	public PropertyValueConfiguration(IBeanConfiguration parentBeanConfiguration, String propertyName, Object value, IProperties props)
	{
		super(parentBeanConfiguration, props);
		this.propertyName = propertyName;
		this.value = value;
	}

	@Override
	public String getPropertyName()
	{
		return propertyName;
	}

	@Override
	public String getFromContext()
	{
		return null;
	}

	@Override
	public String getBeanName()
	{
		return null;
	}

	@Override
	public boolean isOptional()
	{
		return false;
	}

	@Override
	public Object getValue()
	{
		return value;
	}
}
