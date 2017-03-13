package com.koch.ambeth.ioc.config;

import com.koch.ambeth.util.config.IProperties;

public class PropertyRefConfiguration extends AbstractPropertyConfiguration
{
	protected String propertyName;

	protected String beanName;

	protected boolean optional;

	private String fromContext;

	public PropertyRefConfiguration(IBeanConfiguration parentBeanConfiguration, String propertyName, String fromContext, String beanName, boolean optional,
			IProperties props)
	{
		super(parentBeanConfiguration, props);
		this.propertyName = propertyName;
		this.fromContext = fromContext;
		this.beanName = beanName;
		this.optional = optional;
	}

	@Override
	public String getPropertyName()
	{
		return propertyName;
	}

	@Override
	public String getFromContext()
	{
		return fromContext;
	}

	@Override
	public String getBeanName()
	{
		return beanName;
	}

	@Override
	public boolean isOptional()
	{
		return optional;
	}

	@Override
	public Object getValue()
	{
		return null;
	}
}
