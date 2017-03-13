package com.koch.ambeth.ioc.config;

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
