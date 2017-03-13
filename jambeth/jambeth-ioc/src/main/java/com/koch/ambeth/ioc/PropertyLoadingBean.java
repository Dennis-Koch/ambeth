package com.koch.ambeth.ioc;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.config.IProperties;

/**
 * Register this bean in a target IOC container to configure his properties (to be used by other beans of this container)
 */
public class PropertyLoadingBean implements IPropertyLoadingBean
{
	public static final String PROPERTIES_NAME = "Properties";

	@Property
	protected IProperties properties;

	@Override
	public void applyProperties(Properties properties)
	{
		for (String propertyName : this.properties.collectAllPropertyKeys())
		{
			properties.put(propertyName, this.properties.get(propertyName));
		}
	}
}
