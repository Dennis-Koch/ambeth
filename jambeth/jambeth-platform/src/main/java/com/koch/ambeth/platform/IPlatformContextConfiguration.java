package com.koch.ambeth.platform;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.util.config.IProperties;

public interface IPlatformContextConfiguration
{
	IPlatformContextConfiguration addProperties(IProperties... properties);

	IPlatformContextConfiguration addProperties(java.util.Properties... properties);

	IPlatformContextConfiguration addProviderModule(Class<?>... providerModuleTypes);

	IPlatformContextConfiguration addFrameworkModule(Class<?>... frameworkModuleTypes);

	IPlatformContextConfiguration addBootstrapModule(Class<?>... bootstrapModuleTypes);

	IPlatformContextConfiguration addProviderModule(IInitializingModule... providerModules);

	IPlatformContextConfiguration addFrameworkModule(IInitializingModule... frameworkModules);

	IPlatformContextConfiguration addBootstrapModule(IInitializingModule... bootstrapModules);

	IAmbethPlatformContext createPlatformContext();
}