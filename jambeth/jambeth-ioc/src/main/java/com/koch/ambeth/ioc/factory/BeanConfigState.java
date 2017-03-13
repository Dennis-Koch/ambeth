package com.koch.ambeth.ioc.factory;

import com.koch.ambeth.ioc.config.IBeanConfiguration;

public class BeanConfigState
{
	private final IBeanConfiguration beanConfiguration;

	private final Class<?> beanType;

	public BeanConfigState(IBeanConfiguration beanConfiguration, Class<?> beanType)
	{
		this.beanConfiguration = beanConfiguration;
		this.beanType = beanType;
	}

	public IBeanConfiguration getBeanConfiguration()
	{
		return beanConfiguration;
	}

	public Class<?> getBeanType()
	{
		return beanType;
	}
}
