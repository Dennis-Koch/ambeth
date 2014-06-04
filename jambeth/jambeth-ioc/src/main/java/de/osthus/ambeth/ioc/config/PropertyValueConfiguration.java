package de.osthus.ambeth.ioc.config;

import de.osthus.ambeth.config.IProperties;

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