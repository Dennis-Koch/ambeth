package com.koch.ambeth.ioc;

import com.koch.ambeth.log.config.Properties;

public interface IPropertyLoadingBean
{
	void applyProperties(Properties contextProperties);
}
