package com.koch.ambeth.query.ioc;

import com.koch.ambeth.filter.query.service.IGenericQueryService;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.query.filter.GenericQueryService;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;

public class QueryModule implements IInitializingModule {

	@Property(name = ServiceConfigurationConstants.NetworkClientMode, defaultValue = "false")
	protected boolean isNetworkClientMode;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		if (!isNetworkClientMode) {
			beanContextFactory.registerBean(GenericQueryService.class)
					.autowireable(IGenericQueryService.class);
		}
	}
}
