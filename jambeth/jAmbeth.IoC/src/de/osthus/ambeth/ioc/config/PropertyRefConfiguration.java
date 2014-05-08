package de.osthus.ambeth.ioc.config;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.util.ParamChecker;

public class PropertyRefConfiguration extends AbstractPropertyConfiguration
{
	protected String propertyName;

	protected String beanName;

	protected boolean optional;

	public PropertyRefConfiguration(IBeanConfiguration parentBeanConfiguration, String propertyName, String beanName, IProperties props)
	{
		this(parentBeanConfiguration, propertyName, beanName, false, props);
	}

	public PropertyRefConfiguration(IBeanConfiguration parentBeanConfiguration, String propertyName, String beanName, boolean optional, IProperties props)
	{
		super(parentBeanConfiguration, props);
		ParamChecker.assertParamNotNull(propertyName, "propertyName");
		ParamChecker.assertParamNotNull(beanName, "beanName");
		this.propertyName = propertyName;
		this.beanName = beanName;
		this.optional = optional;
	}

	public PropertyRefConfiguration(IBeanConfiguration parentBeanConfiguration, String beanName, IProperties props)
	{
		this(parentBeanConfiguration, beanName, false, props);
	}

	public PropertyRefConfiguration(IBeanConfiguration parentBeanConfiguration, String beanName, boolean optional, IProperties props)
	{
		super(parentBeanConfiguration, props);
		ParamChecker.assertParamNotNull(beanName, "beanName");
		this.beanName = beanName;
		this.optional = optional;
	}

	@Override
	public String getPropertyName()
	{
		return propertyName;
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
