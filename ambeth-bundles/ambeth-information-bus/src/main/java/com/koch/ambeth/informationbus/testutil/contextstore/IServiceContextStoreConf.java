package com.koch.ambeth.informationbus.testutil.contextstore;

import com.koch.ambeth.ioc.IServiceContext;

public interface IServiceContextStoreConf
{
	IServiceContextStoreConf addContext(String name, IServiceContext context);

	IServiceContextStoreConf withConfig(IInterconnectConfig config);

	IServiceContextStoreConf withConfig(Class<? extends IInterconnectConfig> configType);

	IServiceContextStore finish();
}
