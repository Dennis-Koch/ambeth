package de.osthus.ambeth.ioc.config;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.util.ParamChecker;

public class PropertyEmbeddedRefConfiguration extends AbstractPropertyConfiguration
{
	protected String propertyName;

	protected IBeanConfiguration embeddedBean;

	public PropertyEmbeddedRefConfiguration(IBeanConfiguration parentBeanConfiguration, String propertyName, IBeanConfiguration embeddedBean, IProperties props)
	{
		super(parentBeanConfiguration, props);
		ParamChecker.assertParamNotNull(propertyName, "propertyName");
		ParamChecker.assertParamNotNull(embeddedBean, "embeddedBean");
		this.propertyName = propertyName;
		this.embeddedBean = embeddedBean;
	}

	public PropertyEmbeddedRefConfiguration(IBeanConfiguration parentBeanConfiguration, IBeanConfiguration embeddedBean, IProperties props)
	{
		super(parentBeanConfiguration, props);
		ParamChecker.assertParamNotNull(embeddedBean, "embeddedBean");
		this.embeddedBean = embeddedBean;
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
		return embeddedBean.getName();
	}

	@Override
	public boolean isOptional()
	{
		return false;
	}

	@Override
	public Object getValue()
	{
		return embeddedBean;
	}
}
