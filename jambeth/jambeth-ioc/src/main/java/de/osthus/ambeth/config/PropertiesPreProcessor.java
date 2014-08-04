package de.osthus.ambeth.config;

import java.util.List;

import de.osthus.ambeth.ioc.IBeanPreProcessor;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.config.IPropertyConfiguration;
import de.osthus.ambeth.ioc.exception.BeanContextInitException;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;

public class PropertiesPreProcessor implements IBeanPreProcessor, IInitializingBean
{
	@LogInstance
	private ILogger log;

	protected IPropertyInfoProvider propertyInfoProvider;

	protected IConversionHelper conversionHelper;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(propertyInfoProvider, "propertyInfoProvider");
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
	}

	public void setPropertyInfoProvider(IPropertyInfoProvider propertyInfoProvider)
	{
		this.propertyInfoProvider = propertyInfoProvider;
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	@Override
	public void preProcessProperties(IBeanContextFactory beanContextFactory, IProperties props, String beanName, Object service, Class<?> beanType,
			List<IPropertyConfiguration> propertyConfigs, IPropertyInfo[] properties)
	{
		if (properties == null)
		{
			properties = propertyInfoProvider.getProperties(service.getClass());
		}
		for (IPropertyInfo prop : properties)
		{
			if (!prop.isWritable())
			{
				continue;
			}
			Property propertyAttribute = prop.getAnnotation(Property.class);
			if (propertyAttribute == null)
			{
				continue;
			}
			if (Property.DEFAULT_VALUE.equals(propertyAttribute.name()) && Property.DEFAULT_VALUE.equals(propertyAttribute.defaultValue()))
			{
				continue;
			}
			Object value = props.get(propertyAttribute.name());

			if (value == null)
			{
				String stringValue = propertyAttribute.defaultValue();
				if (Property.DEFAULT_VALUE.equals(stringValue))
				{
					if (propertyAttribute.mandatory())
					{
						throw new BeanContextInitException("Could not resolve mandatory environment property '" + propertyAttribute.name() + "'");
					}
					else
					{
						continue;
					}
				}
				else
				{
					value = stringValue;
				}
			}
			value = conversionHelper.convertValueToType(prop.getPropertyType(), value);
			prop.setValue(service, value);
		}
	}
}
