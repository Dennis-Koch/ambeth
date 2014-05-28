package de.osthus.ambeth.ioc;

import de.osthus.ambeth.config.Properties;

public interface IPropertyLoadingBean
{
	void applyProperties(Properties contextProperties);
}
