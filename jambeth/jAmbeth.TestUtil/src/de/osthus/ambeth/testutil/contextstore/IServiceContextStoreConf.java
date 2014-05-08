package de.osthus.ambeth.testutil.contextstore;

import de.osthus.ambeth.ioc.IServiceContext;

public interface IServiceContextStoreConf
{
	IServiceContextStoreConf addContext(String name, IServiceContext context);

	IServiceContextStoreConf withConfig(IInterconnectConfig config);

	IServiceContextStoreConf withConfig(Class<? extends IInterconnectConfig> configType);

	IServiceContextStore finish();
}
